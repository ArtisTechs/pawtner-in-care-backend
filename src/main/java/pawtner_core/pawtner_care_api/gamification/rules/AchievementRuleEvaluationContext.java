package pawtner_core.pawtner_care_api.gamification.rules;

import java.util.Map;

import pawtner_core.pawtner_care_api.gamification.entity.ActivityEvent;
import pawtner_core.pawtner_care_api.gamification.entity.UserStats;

public record AchievementRuleEvaluationContext(
    Map<String, Object> ruleConfig,
    UserStats userStats,
    ActivityEvent activityEvent
) {
}

