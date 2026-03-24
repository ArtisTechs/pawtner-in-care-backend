package pawtner_core.pawtner_care_api.gamification.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import pawtner_core.pawtner_care_api.gamification.enums.ActivityEventType;

public record ActivityEventRequest(
    @NotNull(message = "User id is required")
    UUID userId,

    @NotNull(message = "Event type is required")
    ActivityEventType eventType,

    BigDecimal value,

    String metadata
) {
}
