package pawtner_core.pawtner_care_api.gamification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProgressItemResponse(
    UUID achievementId,
    String achievementCode,
    String title,
    String description,
    String iconUrl,
    Integer points,
    Long progressCurrent,
    Long progressTarget,
    Boolean isUnlocked,
    LocalDateTime unlockedAt,
    Boolean isRepeatable
) {
}

