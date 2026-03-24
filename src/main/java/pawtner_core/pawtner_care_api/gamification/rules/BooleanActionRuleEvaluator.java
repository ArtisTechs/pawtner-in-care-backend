package pawtner_core.pawtner_care_api.gamification.rules;

import org.springframework.stereotype.Component;

import pawtner_core.pawtner_care_api.gamification.enums.AchievementRuleType;

@Component
public class BooleanActionRuleEvaluator extends AbstractAchievementRuleEvaluator {

    @Override
    public AchievementRuleType supports() {
        return AchievementRuleType.BOOLEAN_ACTION;
    }

    @Override
    public RuleProgress evaluate(AchievementRuleEvaluationContext context) {
        String statField = resolveStatField(context.ruleConfig(), "isRegistered");
        boolean unlocked = resolveBooleanStat(context.userStats(), statField);
        return new RuleProgress(unlocked ? 1L : 0L, 1L, unlocked);
    }
}

