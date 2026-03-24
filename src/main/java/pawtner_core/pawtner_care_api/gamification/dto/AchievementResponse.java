package pawtner_core.pawtner_care_api.gamification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import pawtner_core.pawtner_care_api.gamification.enums.AchievementAssignmentType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementCategory;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementRarity;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementRuleType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementTriggerType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementVisibility;

public record AchievementResponse(
    UUID id,
    String code,
    String title,
    String description,
    String iconUrl,
    AchievementCategory category,
    Integer points,
    AchievementRarity rarity,
    Boolean isActive,
    Boolean isRepeatable,
    AchievementVisibility visibility,
    AchievementAssignmentType assignmentType,
    AchievementTriggerType triggerType,
    AchievementRuleType ruleType,
    String ruleConfig,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

