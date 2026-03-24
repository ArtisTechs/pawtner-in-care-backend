package pawtner_core.pawtner_care_api.gamification.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import pawtner_core.pawtner_care_api.gamification.enums.ActivityEventType;

public record ActivityEventResponse(
    UUID id,
    UUID userId,
    ActivityEventType eventType,
    BigDecimal value,
    String metadata,
    LocalDateTime createdAt
) {
}

