package pawtner_core.pawtner_care_api.pet.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.gamification.service.GamificationIntegrationService;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.pet.dto.AdoptionRequestCreateRequest;
import pawtner_core.pawtner_care_api.pet.dto.AdoptionRequestPetResponse;
import pawtner_core.pawtner_care_api.pet.dto.AdoptionRequestResponse;
import pawtner_core.pawtner_care_api.pet.dto.AdoptionRequestStatusUpdateRequest;
import pawtner_core.pawtner_care_api.pet.dto.AdoptionRequestUserResponse;
import pawtner_core.pawtner_care_api.pet.entity.AdoptionRequest;
import pawtner_core.pawtner_care_api.pet.entity.Pet;
import pawtner_core.pawtner_care_api.pet.enums.AdoptionRequestStatus;
import pawtner_core.pawtner_care_api.pet.enums.PetStatus;
import pawtner_core.pawtner_care_api.pet.repository.AdoptionRequestRepository;
import pawtner_core.pawtner_care_api.pet.repository.PetRepository;
import pawtner_core.pawtner_care_api.user.entity.User;
import pawtner_core.pawtner_care_api.user.enums.UserRole;

@Service
public class AdoptionRequestService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "requestNumber",
        "status",
        "createdAt",
        "updatedAt",
        "reviewedAt",
        "pet.name",
        "requester.firstName",
        "requester.lastName",
        "requester.email"
    );

    private final AdoptionRequestRepository adoptionRequestRepository;
    private final PetRepository petRepository;
    private final PetService petService;
    private final GamificationIntegrationService gamificationIntegrationService;

    public AdoptionRequestService(
        AdoptionRequestRepository adoptionRequestRepository,
        PetRepository petRepository,
        PetService petService,
        GamificationIntegrationService gamificationIntegrationService
    ) {
        this.adoptionRequestRepository = adoptionRequestRepository;
        this.petRepository = petRepository;
        this.petService = petService;
        this.gamificationIntegrationService = gamificationIntegrationService;
    }

    @Transactional
    public PageResponse<AdoptionRequestResponse> getAdoptionRequests(
        String search,
        UUID petId,
        UUID requesterId,
        AdoptionRequestStatus status,
        String requestNumber,
        String petName,
        String requesterName,
        String requesterEmail,
        int page,
        int size,
        String sortBy,
        String sortDir,
        boolean ignorePagination
    ) {
        ensureRequestNumbersAssigned();

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction direction = normalizeSortDirection(sortDir);
        Sort sort = Sort.by(direction, normalizedSortBy);
        Specification<AdoptionRequest> specification = buildAdoptionRequestSpecification(
            search,
            petId,
            requesterId,
            status,
            requestNumber,
            petName,
            requesterName,
            requesterEmail
        );

        if (ignorePagination) {
            List<AdoptionRequestResponse> content = adoptionRequestRepository.findAll(specification, sort).stream()
                .map(this::toResponse)
                .toList();
            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<AdoptionRequestResponse> responsePage = adoptionRequestRepository.findAll(specification, pageable)
            .map(this::toResponse);
        return PageResponse.fromPage(responsePage, normalizedSortBy, direction.name().toLowerCase(), false);
    }

    @Transactional
    public AdoptionRequestResponse createRequest(UUID petId, UUID requesterId, AdoptionRequestCreateRequest request) {
        ensureRequestNumbersAssigned();

        Pet pet = petService.findPetEntity(petId);
        User requester = petService.findUserEntity(requesterId);

        validatePetCanReceiveRequests(pet);

        if (pet.getAdoptedBy() != null && pet.getAdoptedBy().getId().equals(requesterId)) {
            throw new IllegalArgumentException("You have already adopted this pet");
        }

        if (adoptionRequestRepository.existsByPetIdAndRequesterIdAndStatus(petId, requesterId, AdoptionRequestStatus.PENDING)) {
            throw new IllegalArgumentException("You already have a pending adoption request for this pet");
        }

        AdoptionRequest adoptionRequest = adoptionRequestRepository.findByPetIdAndRequesterId(petId, requesterId)
            .orElseGet(AdoptionRequest::new);

        adoptionRequest.setPet(pet);
        adoptionRequest.setRequester(requester);
        adoptionRequest.setStatus(AdoptionRequestStatus.PENDING);
        adoptionRequest.setMessage(normalizeOptionalText(request.message()));
        adoptionRequest.setReviewNotes(null);
        adoptionRequest.setReviewedAt(null);
        ensureRequestNumber(adoptionRequest);

        AdoptionRequest savedRequest = adoptionRequestRepository.save(adoptionRequest);
        syncPetStatusWithRequests(pet);

        return toResponse(savedRequest);
    }

    @Transactional
    public List<AdoptionRequestResponse> getRequestsByPet(UUID petId) {
        ensureRequestNumbersAssigned();
        petService.findPetEntity(petId);
        return adoptionRequestRepository.findByPetIdOrderByCreatedAtDesc(petId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public List<AdoptionRequestResponse> getRequestsByUser(UUID userId) {
        ensureRequestNumbersAssigned();
        petService.findUserEntity(userId);
        return adoptionRequestRepository.findByRequesterIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public AdoptionRequestResponse updateRequestStatus(
        UUID requestId,
        UUID currentUserId,
        AdoptionRequestStatusUpdateRequest request
    ) {
        ensureRequestNumbersAssigned();

        AdoptionRequest adoptionRequest = findRequest(requestId);
        User actingUser = petService.findUserEntity(currentUserId);
        AdoptionRequestStatus targetStatus = request.status();

        validateStatusTransition(adoptionRequest, actingUser, targetStatus);

        adoptionRequest.setStatus(targetStatus);
        adoptionRequest.setReviewNotes(normalizeOptionalText(request.reviewNotes()));
        adoptionRequest.setReviewedAt(LocalDateTime.now());

        if (targetStatus == AdoptionRequestStatus.APPROVED) {
            approveRequest(adoptionRequest);
        } else {
            syncPetStatusWithRequests(adoptionRequest.getPet());
        }

        return toResponse(adoptionRequestRepository.save(adoptionRequest));
    }

    private void ensureRequestNumbersAssigned() {
        List<AdoptionRequest> allRequests = adoptionRequestRepository.findAllByOrderByCreatedAtAsc();
        int currentMax = allRequests.stream()
            .map(AdoptionRequest::getRequestNumber)
            .filter(this::isSequentialRequestNumber)
            .mapToInt(Integer::parseInt)
            .max()
            .orElse(0);

        List<AdoptionRequest> requestsToUpdate = new ArrayList<>();
        for (AdoptionRequest adoptionRequest : allRequests) {
            if (isSequentialRequestNumber(adoptionRequest.getRequestNumber())) {
                continue;
            }

            currentMax++;
            adoptionRequest.setRequestNumber(formatRequestNumber(currentMax));
            requestsToUpdate.add(adoptionRequest);
        }

        if (!requestsToUpdate.isEmpty()) {
            adoptionRequestRepository.saveAll(requestsToUpdate);
        }
    }

    private void ensureRequestNumber(AdoptionRequest adoptionRequest) {
        if (isSequentialRequestNumber(adoptionRequest.getRequestNumber())) {
            return;
        }

        int nextNumber = adoptionRequestRepository.findAllByOrderByCreatedAtAsc().stream()
            .map(AdoptionRequest::getRequestNumber)
            .filter(this::isSequentialRequestNumber)
            .mapToInt(Integer::parseInt)
            .max()
            .orElse(0) + 1;

        adoptionRequest.setRequestNumber(formatRequestNumber(nextNumber));
    }

    private Specification<AdoptionRequest> buildAdoptionRequestSpecification(
        String search,
        UUID petId,
        UUID requesterId,
        AdoptionRequestStatus status,
        String requestNumber,
        String petName,
        String requesterName,
        String requesterEmail
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Object, Object> petJoin = root.join("pet");
            Join<Object, Object> requesterJoin = root.join("requester");

            if (petId != null) {
                predicates.add(criteriaBuilder.equal(petJoin.get("id"), petId));
            }

            if (requesterId != null) {
                predicates.add(criteriaBuilder.equal(requesterJoin.get("id"), requesterId));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            addLikePredicate(predicates, criteriaBuilder, root.get("requestNumber"), requestNumber);
            addLikePredicate(predicates, criteriaBuilder, petJoin.get("name"), petName);
            addLikePredicate(predicates, criteriaBuilder, requesterJoin.get("email"), requesterEmail);

            String normalizedRequesterName = normalizeFilter(requesterName);
            if (normalizedRequesterName != null) {
                String pattern = "%" + normalizedRequesterName.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(requesterJoin.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(requesterJoin.get("middleName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(requesterJoin.get("lastName")), pattern)
                    )
                );
            }

            String normalizedSearch = normalizeFilter(search);
            if (normalizedSearch != null) {
                String pattern = "%" + normalizedSearch.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("requestNumber")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("message")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("reviewNotes")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(petJoin.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(requesterJoin.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(requesterJoin.get("middleName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(requesterJoin.get("lastName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(requesterJoin.get("email")), pattern)
                    )
                );
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validatePetCanReceiveRequests(Pet pet) {
        if (pet.getStatus() == PetStatus.ADOPTED) {
            throw new IllegalArgumentException("This pet has already been adopted");
        }
    }

    private void validateStatusTransition(AdoptionRequest adoptionRequest, User actingUser, AdoptionRequestStatus targetStatus) {
        if (adoptionRequest.getStatus() != AdoptionRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending adoption requests can be updated");
        }

        if (targetStatus == AdoptionRequestStatus.PENDING) {
            throw new IllegalArgumentException("Adoption request is already pending");
        }

        if (targetStatus == AdoptionRequestStatus.CANCELLED) {
            if (!adoptionRequest.getRequester().getId().equals(actingUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own adoption requests");
            }
            return;
        }

        if (targetStatus != AdoptionRequestStatus.APPROVED && targetStatus != AdoptionRequestStatus.REJECTED) {
            throw new IllegalArgumentException("Unsupported adoption request status update");
        }

        if (actingUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can approve or reject adoption requests");
        }
    }

    private void approveRequest(AdoptionRequest adoptionRequest) {
        Pet pet = adoptionRequest.getPet();
        pet.setStatus(PetStatus.ADOPTED);
        pet.setAdoptedBy(adoptionRequest.getRequester());
        pet.setAdoptionDate(LocalDate.now());
        petRepository.save(pet);

        List<AdoptionRequest> otherPendingRequests = adoptionRequestRepository.findByPetIdAndStatus(
            pet.getId(),
            AdoptionRequestStatus.PENDING
        );

        for (AdoptionRequest pendingRequest : otherPendingRequests) {
            if (pendingRequest.getId().equals(adoptionRequest.getId())) {
                continue;
            }

            pendingRequest.setStatus(AdoptionRequestStatus.REJECTED);
            pendingRequest.setReviewNotes("Another adoption request was approved for this pet");
            pendingRequest.setReviewedAt(LocalDateTime.now());
            adoptionRequestRepository.save(pendingRequest);
        }

        gamificationIntegrationService.onPetAdopted(adoptionRequest.getRequester().getId());
    }

    private void syncPetStatusWithRequests(Pet pet) {
        if (pet.getStatus() == PetStatus.ADOPTED) {
            return;
        }

        boolean hasPendingRequests = !adoptionRequestRepository.findByPetIdAndStatus(
            pet.getId(),
            AdoptionRequestStatus.PENDING
        ).isEmpty();

        if (hasPendingRequests) {
            if (pet.getStatus() != PetStatus.ONGOING_ADOPTION) {
                pet.setStatus(PetStatus.ONGOING_ADOPTION);
                petRepository.save(pet);
            }
            return;
        }

        boolean petNeedsAvailabilityReset = pet.getStatus() != PetStatus.AVAILABLE_FOR_ADOPTION
            || pet.getAdoptedBy() != null
            || pet.getAdoptionDate() != null;

        if (petNeedsAvailabilityReset) {
            pet.setStatus(PetStatus.AVAILABLE_FOR_ADOPTION);
            pet.setAdoptedBy(null);
            pet.setAdoptionDate(null);
            petRepository.save(pet);
        }
    }

    private AdoptionRequest findRequest(UUID requestId) {
        return adoptionRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Adoption request with id " + requestId + " was not found"));
    }

    private AdoptionRequestResponse toResponse(AdoptionRequest adoptionRequest) {
        return new AdoptionRequestResponse(
            adoptionRequest.getId(),
            adoptionRequest.getRequestNumber(),
            toPetResponse(adoptionRequest.getPet()),
            toUserResponse(adoptionRequest.getRequester()),
            adoptionRequest.getStatus(),
            adoptionRequest.getMessage(),
            adoptionRequest.getReviewNotes(),
            adoptionRequest.getReviewedAt(),
            adoptionRequest.getCreatedAt(),
            adoptionRequest.getUpdatedAt()
        );
    }

    private AdoptionRequestPetResponse toPetResponse(Pet pet) {
        return new AdoptionRequestPetResponse(
            pet.getId(),
            pet.getName(),
            pet.getType(),
            pet.getRace(),
            pet.getStatus(),
            pet.getPhoto()
        );
    }

    private AdoptionRequestUserResponse toUserResponse(User user) {
        return new AdoptionRequestUserResponse(
            user.getId(),
            user.getFirstName(),
            user.getMiddleName(),
            user.getLastName(),
            user.getEmail(),
            user.getProfilePicture()
        );
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isSequentialRequestNumber(String requestNumber) {
        return requestNumber != null && requestNumber.matches("\\d{6}");
    }

    private String formatRequestNumber(int value) {
        return String.format("%06d", value);
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
}
