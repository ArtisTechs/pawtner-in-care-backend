package pawtner_core.pawtner_care_api.gamification.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementAssignmentType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementCategory;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementRarity;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementRuleType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementTriggerType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementVisibility;

public record AchievementUpsertRequest(
    @NotBlank(message = "Code is required")
    @Size(max = 100, message = "Code must not exceed 100 characters")
    String code,

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    String title,

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    String iconUrl,

    @NotNull(message = "Category is required")
    AchievementCategory category,

    @NotNull(message = "Points are required")
    @Min(value = 0, message = "Points must be greater than or equal to 0")
    Integer points,

    @NotNull(message = "Rarity is required")
    AchievementRarity rarity,

    @NotNull(message = "Active status is required")
    Boolean isActive,

    @NotNull(message = "Repeatable flag is required")
    Boolean isRepeatable,

    @NotNull(message = "Visibility is required")
    AchievementVisibility visibility,

    @NotNull(message = "Assignment type is required")
    AchievementAssignmentType assignmentType,

    @NotNull(message = "Trigger type is required")
    AchievementTriggerType triggerType,

    @NotNull(message = "Rule type is required")
    AchievementRuleType ruleType,

    String ruleConfig,

    LocalDateTime startAt,

    LocalDateTime endAt
) {
}
