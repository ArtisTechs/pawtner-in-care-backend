package pawtner_core.pawtner_care_api.pet.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public AdoptionRequestResponse createRequest(UUID petId, UUID requesterId, AdoptionRequestCreateRequest request) {
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

        AdoptionRequest savedRequest = adoptionRequestRepository.save(adoptionRequest);

        if (pet.getStatus() == PetStatus.AVAILABLE_FOR_ADOPTION) {
            pet.setStatus(PetStatus.ONGOING_ADOPTION);
            petRepository.save(pet);
        }

        return toResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<AdoptionRequestResponse> getRequestsByPet(UUID petId) {
        petService.findPetEntity(petId);
        return adoptionRequestRepository.findByPetIdOrderByCreatedAtDesc(petId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AdoptionRequestResponse> getRequestsByUser(UUID userId) {
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
        AdoptionRequest adoptionRequest = findRequest(requestId);
        User actingUser = petService.findUserEntity(currentUserId);
        AdoptionRequestStatus targetStatus = request.status();

        validateStatusTransition(adoptionRequest, actingUser, targetStatus);

        adoptionRequest.setStatus(targetStatus);
        adoptionRequest.setReviewNotes(normalizeOptionalText(request.reviewNotes()));
        adoptionRequest.setReviewedAt(LocalDateTime.now());

        if (targetStatus == AdoptionRequestStatus.APPROVED) {
            approveRequest(adoptionRequest);
        } else if (targetStatus == AdoptionRequestStatus.REJECTED || targetStatus == AdoptionRequestStatus.CANCELLED) {
            resetPetAvailabilityIfNeeded(adoptionRequest.getPet());
        }

        return toResponse(adoptionRequestRepository.save(adoptionRequest));
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

    private void resetPetAvailabilityIfNeeded(Pet pet) {
        if (pet.getStatus() == PetStatus.ADOPTED) {
            return;
        }

        boolean hasPendingRequests = adoptionRequestRepository.findByPetIdAndStatus(pet.getId(), AdoptionRequestStatus.PENDING)
            .stream()
            .anyMatch(request -> request.getStatus() == AdoptionRequestStatus.PENDING);

        if (!hasPendingRequests) {
            pet.setStatus(PetStatus.AVAILABLE_FOR_ADOPTION);
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
            user.getEmail()
        );
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
