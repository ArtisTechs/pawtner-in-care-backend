package pawtner_core.pawtner_care_api.pet.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.pet.dto.AdoptionRequestCreateRequest;
import pawtner_core.pawtner_care_api.pet.dto.AdoptionRequestResponse;
import pawtner_core.pawtner_care_api.pet.dto.AdoptionRequestStatusUpdateRequest;
import pawtner_core.pawtner_care_api.pet.service.AdoptionRequestService;

@RestController
public class AdoptionRequestController {

    private final AdoptionRequestService adoptionRequestService;

    public AdoptionRequestController(AdoptionRequestService adoptionRequestService) {
        this.adoptionRequestService = adoptionRequestService;
    }

    @PostMapping("/api/pets/{petId}/adoption-requests")
    public ResponseEntity<AdoptionRequestResponse> createAdoptionRequest(
        @PathVariable UUID petId,
        @RequestHeader("X-User-Id") UUID currentUserId,
        @Valid @RequestBody AdoptionRequestCreateRequest request
    ) {
        AdoptionRequestResponse response = adoptionRequestService.createRequest(petId, currentUserId, request);
        return ResponseEntity.created(URI.create("/api/adoption-requests/" + response.id())).body(response);
    }

    @GetMapping("/api/pets/{petId}/adoption-requests")
    public List<AdoptionRequestResponse> getAdoptionRequestsByPet(@PathVariable UUID petId) {
        return adoptionRequestService.getRequestsByPet(petId);
    }

    @GetMapping("/api/users/{userId}/adoption-requests")
    public List<AdoptionRequestResponse> getAdoptionRequestsByUser(@PathVariable UUID userId) {
        return adoptionRequestService.getRequestsByUser(userId);
    }

    @PatchMapping("/api/adoption-requests/{requestId}/status")
    public AdoptionRequestResponse updateAdoptionRequestStatus(
        @PathVariable UUID requestId,
        @RequestHeader("X-User-Id") UUID currentUserId,
        @Valid @RequestBody AdoptionRequestStatusUpdateRequest request
    ) {
        return adoptionRequestService.updateRequestStatus(requestId, currentUserId, request);
    }
}
