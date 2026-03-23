package pawtner_core.pawtner_care_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import pawtner_core.pawtner_care_api.enums.EmergencySosStatus;
import pawtner_core.pawtner_care_api.enums.EmergencySosType;

public record EmergencySosResponse(
    UUID id,
    UUID personFilledId,
    String personFilledFullName,
    String personFilledEmail,
    EmergencySosType type,
    String addressLocation,
    BigDecimal latitude,
    @JsonProperty("long")
    BigDecimal longitude,
    String additionalLocationMessage,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    EmergencySosStatus status
) {
}
