package pawtner_core.pawtner_care_api.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserActiveUpdateRequest(
    @NotNull(message = "Active status is required")
    Boolean active
) {
}
