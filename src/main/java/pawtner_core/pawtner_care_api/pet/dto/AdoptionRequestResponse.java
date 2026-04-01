package pawtner_core.pawtner_care_api.pet.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import pawtner_core.pawtner_care_api.pet.enums.AdoptionRequestStatus;

public record AdoptionRequestResponse(
    UUID id,
    String requestNumber,
    AdoptionRequestPetResponse pet,
    AdoptionRequestUserResponse requester,
    AdoptionRequestStatus status,
    String message,
    String reviewNotes,
    LocalDateTime reviewedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
