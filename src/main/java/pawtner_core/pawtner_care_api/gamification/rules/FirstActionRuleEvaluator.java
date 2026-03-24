package pawtner_core.pawtner_care_api.gamification.rules;

import org.springframework.stereotype.Component;

import pawtner_core.pawtner_care_api.gamification.enums.AchievementRuleType;

@Component
public class FirstActionRuleEvaluator extends AbstractAchievementRuleEvaluator {

    @Override
    public AchievementRuleType supports() {
        return AchievementRuleType.FIRST_ACTION;
    }

    @Override
    public RuleProgress evaluate(AchievementRuleEvaluationContext context) {
        String statField = resolveStatField(context.ruleConfig(), "totalAdoptedPets");
        long current = resolveNumericStat(context.userStats(), statField);
        return new RuleProgress(current, 1L, current >= 1L);
    }
}

