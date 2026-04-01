package pawtner_core.pawtner_care_api.pet.dto;

import jakarta.validation.constraints.Size;

public record AdoptionRequestCreateRequest(
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    String message
) {
}
