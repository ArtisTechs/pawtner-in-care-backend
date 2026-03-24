package pawtner_core.pawtner_care_api.gamification.dto;

import jakarta.validation.constraints.NotNull;

public record AchievementStatusUpdateRequest(
    @NotNull(message = "Active status is required")
    Boolean isActive
) {
}
