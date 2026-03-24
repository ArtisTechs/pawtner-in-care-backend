package pawtner_core.pawtner_care_api.gamification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HeroesWallEntryResponse(
    UUID userId,
    String displayName,
    String email,
    Long totalPoints,
    Long unlockedAchievements,
    LocalDateTime latestUnlockedAt,
    Integer rank
) {
}
