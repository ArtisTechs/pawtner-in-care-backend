package pawtner_core.pawtner_care_api.gamification.dto;

import java.util.List;
import java.util.UUID;

public record UserPublicHeroesWallResponse(
    UUID userId,
    GamificationUserProfileResponse profile,
    Long totalPoints,
    Long unlockedAchievements,
    List<UserAchievementResponse> achievements
) {
}

