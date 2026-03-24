package pawtner_core.pawtner_care_api.gamification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserAchievementResponse(
    UUID id,
    UUID userId,
    AchievementResponse achievement,
    Long progressCurrent,
    Long progressTarget,
    Boolean isUnlocked,
    LocalDateTime unlockedAt,
    String sourceEvent,
    String metadata,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
