package pawtner_core.pawtner_care_api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pawtner_core.pawtner_care_api.enums.EmergencySosStatus;
import pawtner_core.pawtner_care_api.enums.EmergencySosType;

public record EmergencySosRequest(
    @NotNull(message = "Person filled is required")
    UUID personFilledId,

    @NotNull(message = "Type is required")
    EmergencySosType type,

    @NotBlank(message = "Address location is required")
    @Size(max = 255, message = "Address location must not exceed 255 characters")
    String addressLocation,

    @NotNull(message = "Latitude is required")
    BigDecimal latitude,

    @NotNull(message = "Longitude is required")
    @JsonProperty("long")
    BigDecimal longitude,

    @Size(max = 1000, message = "Additional location message must not exceed 1000 characters")
    String additionalLocationMessage,

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description,

    @NotNull(message = "Status is required")
    EmergencySosStatus status
) {
}
