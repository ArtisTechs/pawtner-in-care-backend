package pawtner_core.pawtner_care_api.gamification.rules;

import org.springframework.stereotype.Component;

import pawtner_core.pawtner_care_api.gamification.enums.AchievementRuleType;

@Component
public class CountThresholdRuleEvaluator extends AbstractAchievementRuleEvaluator {

    @Override
    public AchievementRuleType supports() {
        return AchievementRuleType.COUNT_THRESHOLD;
    }

    @Override
    public RuleProgress evaluate(AchievementRuleEvaluationContext context) {
        String statField = resolveStatField(context.ruleConfig(), "totalDonations");
        long current = resolveNumericStat(context.userStats(), statField);
        long target = resolveTarget(context.ruleConfig(), 1L);
        return new RuleProgress(current, target, current >= target);
    }
}

