package pawtner_core.pawtner_care_api.pet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pawtner_core.pawtner_care_api.pet.enums.AdoptionRequestStatus;

public record AdoptionRequestStatusUpdateRequest(
    @NotNull(message = "Status is required")
    AdoptionRequestStatus status,
    @Size(max = 1000, message = "Review notes must not exceed 1000 characters")
    String reviewNotes
) {
}
