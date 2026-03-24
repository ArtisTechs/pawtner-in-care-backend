package pawtner_core.pawtner_care_api.gamification.dto;

import java.util.List;
import java.util.UUID;

public record UserProgressViewResponse(
    UUID userId,
    GamificationUserProfileResponse profile,
    UserStatsResponse stats,
    Long totalPoints,
    List<UserProgressItemResponse> achievements
) {
}
