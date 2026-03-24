package pawtner_core.pawtner_care_api.gamification.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ManualAchievementAssignmentRequest(
    @NotNull(message = "User id is required")
    UUID userId,

    UUID achievementId,

    String achievementCode,

    String metadata
) {
}
