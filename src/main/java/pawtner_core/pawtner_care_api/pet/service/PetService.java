package pawtner_core.pawtner_care_api.pet.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.pet.dto.PetAdopterResponse;
import pawtner_core.pawtner_care_api.pet.dto.PetRequest;
import pawtner_core.pawtner_care_api.pet.dto.PetResponse;
import pawtner_core.pawtner_care_api.pet.entity.Pet;
import pawtner_core.pawtner_care_api.user.entity.User;
import pawtner_core.pawtner_care_api.pet.enums.PetStatus;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.pet.repository.PetRepository;
import pawtner_core.pawtner_care_api.user.repository.UserRepository;

@Service
public class PetService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id",
        "name",
        "gender",
        "weight",
        "height",
        "birthDate",
        "adoptionDate",
        "rescuedDate",
        "isVaccinated",
        "type",
        "status"
    );

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public PetService(PetRepository petRepository, UserRepository userRepository) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PetResponse> getPets(
        String search,
        String name,
        String gender,
        String type,
        PetStatus status,
        Boolean isVaccinated,
        LocalDate birthDateFrom,
        LocalDate birthDateTo,
        int page,
        int size,
        String sortBy,
        String sortDir,
        boolean ignorePagination
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction direction = normalizeSortDirection(sortDir);
        Sort sort = Sort.by(direction, normalizedSortBy);
        Specification<Pet> specification = buildPetSpecification(
            search,
            name,
            gender,
            type,
            status,
            isVaccinated,
            birthDateFrom,
            birthDateTo
        );

        if (ignorePagination) {
            List<PetResponse> content = petRepository.findAll(specification, sort).stream()
                .map(this::toPetResponse)
                .toList();
            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<PetResponse> responsePage = petRepository.findAll(specification, pageable).map(this::toPetResponse);
        return PageResponse.fromPage(responsePage, normalizedSortBy, direction.name().toLowerCase(), false);
    }

    @Transactional(readOnly = true)
    public PetResponse getPet(UUID id) {
        return toPetResponse(findPetEntity(id));
    }

    @Transactional
    public PetResponse createPet(PetRequest request) {
        validateDates(request);

        Pet pet = new Pet();
        applyRequest(pet, request);

        return toPetResponse(petRepository.save(pet));
    }

    @Transactional
    public PetResponse updatePet(UUID id, PetRequest request) {
        validateDates(request);

        Pet pet = findPetEntity(id);
        applyRequest(pet, request);

        return toPetResponse(petRepository.save(pet));
    }

    @Transactional
    public void deletePet(UUID id) {
        Pet pet = findPetEntity(id);
        petRepository.delete(pet);
    }

    public Pet findPetEntity(UUID id) {
        return petRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pet with id " + id + " was not found"));
    }

    private Specification<Pet> buildPetSpecification(
        String search,
        String name,
        String gender,
        String type,
        PetStatus status,
        Boolean isVaccinated,
        LocalDate birthDateFrom,
        LocalDate birthDateTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            addLikePredicate(predicates, criteriaBuilder, root.get("name"), name);
            addLikePredicate(predicates, criteriaBuilder, root.get("gender"), gender);
            addLikePredicate(predicates, criteriaBuilder, root.get("type"), type);

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (isVaccinated != null) {
                predicates.add(criteriaBuilder.equal(root.get("isVaccinated"), isVaccinated));
            }

            if (birthDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthDate"), birthDateFrom));
            }

            if (birthDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("birthDate"), birthDateTo));
            }

            String normalizedSearch = normalizeFilter(search);
            if (normalizedSearch != null) {
                String pattern = "%" + normalizedSearch.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("gender")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("type")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                    )
                );
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void applyRequest(Pet pet, PetRequest request) {
        pet.setName(request.name().trim());
        pet.setGender(request.gender().trim());
        pet.setWeight(request.weight());
        pet.setHeight(request.height());
        pet.setBirthDate(request.birthDate());
        pet.setAdoptionDate(request.adoptionDate());
        pet.setAdoptedBy(resolveAdoptedBy(request.adoptedById()));
        pet.setRescuedDate(request.rescuedDate());
        pet.setDescription(normalizeOptionalText(request.description()));
        pet.setPhoto(normalizeOptionalText(request.photo()));
        pet.setVideos(normalizeOptionalText(request.videos()));
        pet.setIsVaccinated(request.isVaccinated());
        pet.setType(request.type().trim());
        pet.setRace(normalizeOptionalText(request.race()));
        pet.setStatus(request.status());
    }

    private void validateDates(PetRequest request) {
        LocalDate birthDate = request.birthDate();
        LocalDate adoptionDate = request.adoptionDate();
        LocalDate rescuedDate = request.rescuedDate();

        if (birthDate != null && adoptionDate != null && adoptionDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("Adoption date cannot be earlier than birth date");
        }

        if (birthDate != null && rescuedDate != null && rescuedDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("Rescued date cannot be earlier than birth date");
        }
    }

    private void addLikePredicate(
        List<Predicate> predicates,
        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
        jakarta.persistence.criteria.Path<String> path,
        String value
    ) {
        String normalizedValue = normalizeFilter(value);
        if (normalizedValue != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(path), "%" + normalizedValue.toLowerCase() + "%"));
        }
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private String normalizeSortBy(String sortBy) {
        String requestedSortBy = normalizeFilter(sortBy);
        if (requestedSortBy == null) {
            return "id";
        }

        if (!ALLOWED_SORT_FIELDS.contains(requestedSortBy)) {
            throw new IllegalArgumentException("Invalid sortBy value: " + requestedSortBy);
        }

        return requestedSortBy;
    }

    private Sort.Direction normalizeSortDirection(String sortDir) {
        String requestedSortDirection = normalizeFilter(sortDir);
        if (requestedSortDirection == null) {
            return Sort.Direction.ASC;
        }

        try {
            return Sort.Direction.fromString(requestedSortDirection);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid sortDir value: " + requestedSortDirection);
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    public PetResponse toPetResponse(Pet pet) {
        return new PetResponse(
            pet.getId(),
            pet.getName(),
            pet.getGender(),
            pet.getWeight(),
            pet.getHeight(),
            pet.getBirthDate(),
            calculateAge(pet.getBirthDate()),
            pet.getAdoptionDate(),
            toAdopterResponse(pet.getAdoptedBy()),
            pet.getRescuedDate(),
            pet.getDescription(),
            pet.getPhoto(),
            pet.getVideos(),
            pet.getIsVaccinated(),
            pet.getType(),
            pet.getRace(),
            pet.getStatus()
        );
    }

    public User findUserEntity(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " was not found"));
    }

    private User resolveAdoptedBy(UUID adoptedById) {
        if (adoptedById == null) {
            return null;
        }

        return findUserEntity(adoptedById);
    }

    private PetAdopterResponse toAdopterResponse(User user) {
        if (user == null) {
            return null;
        }

        return new PetAdopterResponse(
            user.getId(),
            user.getFirstName(),
            user.getMiddleName(),
            user.getLastName(),
            user.getEmail()
        );
    }

    private Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }

        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}

