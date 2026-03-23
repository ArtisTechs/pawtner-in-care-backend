package pawtner_core.pawtner_care_api.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.dto.EmergencySosRequest;
import pawtner_core.pawtner_care_api.dto.EmergencySosResponse;
import pawtner_core.pawtner_care_api.dto.PageResponse;
import pawtner_core.pawtner_care_api.entity.EmergencySos;
import pawtner_core.pawtner_care_api.entity.User;
import pawtner_core.pawtner_care_api.enums.EmergencySosStatus;
import pawtner_core.pawtner_care_api.enums.EmergencySosType;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.repository.EmergencySosRepository;
import pawtner_core.pawtner_care_api.repository.UserRepository;

@Service
public class EmergencySosService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id",
        "type",
        "status",
        "addressLocation",
        "createdAt",
        "updatedAt",
        "personFilled.firstName",
        "personFilled.lastName",
        "personFilled.email"
    );

    private final EmergencySosRepository emergencySosRepository;
    private final UserRepository userRepository;

    public EmergencySosService(EmergencySosRepository emergencySosRepository, UserRepository userRepository) {
        this.emergencySosRepository = emergencySosRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmergencySosResponse> getEmergencySosList(
        String search,
        UUID personFilledId,
        EmergencySosType type,
        EmergencySosStatus status,
        String addressLocation,
        String personFilledName,
        String personFilledEmail,
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
        Specification<EmergencySos> specification = buildEmergencySosSpecification(
            search,
            personFilledId,
            type,
            status,
            addressLocation,
            personFilledName,
            personFilledEmail
        );

        if (ignorePagination) {
            List<EmergencySosResponse> content = emergencySosRepository.findAll(specification, sort).stream()
                .map(this::toResponse)
                .toList();
            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<EmergencySosResponse> responsePage = emergencySosRepository.findAll(specification, pageable).map(this::toResponse);
        return PageResponse.fromPage(responsePage, normalizedSortBy, direction.name().toLowerCase(), false);
    }

    @Transactional(readOnly = true)
    public EmergencySosResponse getEmergencySos(UUID id) {
        return toResponse(findEmergencySos(id));
    }

    @Transactional
    public EmergencySosResponse createEmergencySos(EmergencySosRequest request) {
        EmergencySos emergencySos = new EmergencySos();
        applyRequest(emergencySos, request);

        return toResponse(emergencySosRepository.save(emergencySos));
    }

    @Transactional
    public EmergencySosResponse updateEmergencySos(UUID id, EmergencySosRequest request) {
        EmergencySos emergencySos = findEmergencySos(id);
        applyRequest(emergencySos, request);

        return toResponse(emergencySosRepository.save(emergencySos));
    }

    @Transactional
    public void deleteEmergencySos(UUID id) {
        EmergencySos emergencySos = findEmergencySos(id);
        emergencySosRepository.delete(emergencySos);
    }

    private EmergencySos findEmergencySos(UUID id) {
        return emergencySosRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Emergency SOS with id " + id + " was not found"));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " was not found"));
    }

    private Specification<EmergencySos> buildEmergencySosSpecification(
        String search,
        UUID personFilledId,
        EmergencySosType type,
        EmergencySosStatus status,
        String addressLocation,
        String personFilledName,
        String personFilledEmail
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            Join<EmergencySos, User> personFilledJoin = root.join("personFilled");

            if (personFilledId != null) {
                predicates.add(criteriaBuilder.equal(personFilledJoin.get("id"), personFilledId));
            }

            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            addLikePredicate(predicates, criteriaBuilder, root.get("addressLocation"), addressLocation);
            addLikePredicate(predicates, criteriaBuilder, personFilledJoin.get("email"), personFilledEmail);

            String normalizedPersonFilledName = normalizeFilter(personFilledName);
            if (normalizedPersonFilledName != null) {
                String pattern = "%" + normalizedPersonFilledName.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(personFilledJoin.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(personFilledJoin.get("middleName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(personFilledJoin.get("lastName")), pattern)
                    )
                );
            }

            String normalizedSearch = normalizeFilter(search);
            if (normalizedSearch != null) {
                String pattern = "%" + normalizedSearch.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(personFilledJoin.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(personFilledJoin.get("middleName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(personFilledJoin.get("lastName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(personFilledJoin.get("email")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("addressLocation")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("additionalLocationMessage")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                    )
                );
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void applyRequest(EmergencySos emergencySos, EmergencySosRequest request) {
        emergencySos.setPersonFilled(findUser(request.personFilledId()));
        emergencySos.setType(request.type());
        emergencySos.setAddressLocation(request.addressLocation().trim());
        emergencySos.setLatitude(request.latitude());
        emergencySos.setLongitude(request.longitude());
        emergencySos.setAdditionalLocationMessage(normalizeOptionalText(request.additionalLocationMessage()));
        emergencySos.setDescription(normalizeOptionalText(request.description()));
        emergencySos.setStatus(request.status());
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

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
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
            return "createdAt";
        }

        if (!ALLOWED_SORT_FIELDS.contains(requestedSortBy)) {
            throw new IllegalArgumentException("Invalid sortBy value: " + requestedSortBy);
        }

        return requestedSortBy;
    }

    private Sort.Direction normalizeSortDirection(String sortDir) {
        String requestedSortDirection = normalizeFilter(sortDir);
        if (requestedSortDirection == null) {
            return Sort.Direction.DESC;
        }

        try {
            return Sort.Direction.fromString(requestedSortDirection);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid sortDir value: " + requestedSortDirection);
        }
    }

    private EmergencySosResponse toResponse(EmergencySos emergencySos) {
        User personFilled = emergencySos.getPersonFilled();

        return new EmergencySosResponse(
            emergencySos.getId(),
            personFilled.getId(),
            buildFullName(personFilled),
            personFilled.getEmail(),
            emergencySos.getType(),
            emergencySos.getAddressLocation(),
            emergencySos.getLatitude(),
            emergencySos.getLongitude(),
            emergencySos.getAdditionalLocationMessage(),
            emergencySos.getDescription(),
            emergencySos.getCreatedAt(),
            emergencySos.getUpdatedAt(),
            emergencySos.getStatus()
        );
    }

    private String buildFullName(User user) {
        String middleName = user.getMiddleName();
        if (middleName == null || middleName.isBlank()) {
            return user.getFirstName() + " " + user.getLastName();
        }

        return user.getFirstName() + " " + middleName + " " + user.getLastName();
    }
}
