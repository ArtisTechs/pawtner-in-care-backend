package pawtner_core.pawtner_care_api.gamification.rules;

import pawtner_core.pawtner_care_api.gamification.enums.AchievementRuleType;

public interface AchievementRuleEvaluator {

    AchievementRuleType supports();

    RuleProgress evaluate(AchievementRuleEvaluationContext context);
}

