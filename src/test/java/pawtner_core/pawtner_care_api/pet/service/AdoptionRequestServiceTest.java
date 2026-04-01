package pawtner_core.pawtner_care_api.pet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import pawtner_core.pawtner_care_api.gamification.service.GamificationIntegrationService;
import pawtner_core.pawtner_care_api.pet.dto.AdoptionRequestCreateRequest;
import pawtner_core.pawtner_care_api.pet.dto.AdoptionRequestResponse;
import pawtner_core.pawtner_care_api.pet.dto.AdoptionRequestStatusUpdateRequest;
import pawtner_core.pawtner_care_api.pet.entity.AdoptionRequest;
import pawtner_core.pawtner_care_api.pet.entity.Pet;
import pawtner_core.pawtner_care_api.pet.enums.AdoptionRequestStatus;
import pawtner_core.pawtner_care_api.pet.enums.PetStatus;
import pawtner_core.pawtner_care_api.pet.repository.AdoptionRequestRepository;
import pawtner_core.pawtner_care_api.pet.repository.PetRepository;
import pawtner_core.pawtner_care_api.user.entity.User;
import pawtner_core.pawtner_care_api.user.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class AdoptionRequestServiceTest {

    @Mock
    private AdoptionRequestRepository adoptionRequestRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetService petService;

    @Mock
    private GamificationIntegrationService gamificationIntegrationService;

    @InjectMocks
    private AdoptionRequestService adoptionRequestService;

    @Test
    void createRequestMovesPetToOngoingAdoption() {
        UUID petId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Pet pet = createPet(petId, PetStatus.AVAILABLE_FOR_ADOPTION);
        User requester = createUser(requesterId, UserRole.USER);

        when(petService.findPetEntity(petId)).thenReturn(pet);
        when(petService.findUserEntity(requesterId)).thenReturn(requester);
        when(adoptionRequestRepository.existsByPetIdAndRequesterIdAndStatus(petId, requesterId, AdoptionRequestStatus.PENDING))
            .thenReturn(false);
        when(adoptionRequestRepository.findByPetIdAndRequesterId(petId, requesterId)).thenReturn(Optional.empty());
        when(adoptionRequestRepository.save(any(AdoptionRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdoptionRequestResponse response = adoptionRequestService.createRequest(
            petId,
            requesterId,
            new AdoptionRequestCreateRequest("I can provide a safe home")
        );

        assertEquals(AdoptionRequestStatus.PENDING, response.status());
        assertEquals(PetStatus.ONGOING_ADOPTION, pet.getStatus());
        verify(petRepository).save(pet);
    }

    @Test
    void approveRequestMarksPetAdoptedAndAwardsGamification() {
        UUID requestId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Pet pet = createPet(petId, PetStatus.ONGOING_ADOPTION);
        User requester = createUser(requesterId, UserRole.USER);
        User admin = createUser(adminId, UserRole.ADMIN);

        AdoptionRequest adoptionRequest = new AdoptionRequest();
        adoptionRequest.setId(requestId);
        adoptionRequest.setPet(pet);
        adoptionRequest.setRequester(requester);
        adoptionRequest.setStatus(AdoptionRequestStatus.PENDING);

        when(adoptionRequestRepository.findById(requestId)).thenReturn(Optional.of(adoptionRequest));
        when(petService.findUserEntity(adminId)).thenReturn(admin);
        when(adoptionRequestRepository.findByPetIdAndStatus(petId, AdoptionRequestStatus.PENDING)).thenReturn(List.of(adoptionRequest));
        when(adoptionRequestRepository.save(any(AdoptionRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdoptionRequestResponse response = adoptionRequestService.updateRequestStatus(
            requestId,
            adminId,
            new AdoptionRequestStatusUpdateRequest(AdoptionRequestStatus.APPROVED, "Approved")
        );

        assertEquals(AdoptionRequestStatus.APPROVED, response.status());
        assertEquals(PetStatus.ADOPTED, pet.getStatus());
        assertEquals(requester, pet.getAdoptedBy());
        verify(petRepository).save(pet);
        verify(gamificationIntegrationService).onPetAdopted(requesterId);
    }

    @Test
    void nonAdminCannotApproveRequest() {
        UUID requestId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID actingUserId = UUID.randomUUID();

        AdoptionRequest adoptionRequest = new AdoptionRequest();
        adoptionRequest.setId(requestId);
        adoptionRequest.setPet(createPet(UUID.randomUUID(), PetStatus.ONGOING_ADOPTION));
        adoptionRequest.setRequester(createUser(requesterId, UserRole.USER));
        adoptionRequest.setStatus(AdoptionRequestStatus.PENDING);

        when(adoptionRequestRepository.findById(requestId)).thenReturn(Optional.of(adoptionRequest));
        when(petService.findUserEntity(actingUserId)).thenReturn(createUser(actingUserId, UserRole.USER));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> adoptionRequestService.updateRequestStatus(
                requestId,
                actingUserId,
                new AdoptionRequestStatusUpdateRequest(AdoptionRequestStatus.APPROVED, null)
            )
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(gamificationIntegrationService, never()).onPetAdopted(any());
    }

    private Pet createPet(UUID petId, PetStatus status) {
        Pet pet = new Pet();
        pet.setId(petId);
        pet.setName("Milo");
        pet.setType("Dog");
        pet.setStatus(status);
        return pet;
    }

    private User createUser(UUID userId, UserRole role) {
        User user = new User();
        user.setId(userId);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane@example.com");
        user.setRole(role);
        return user;
    }
}
