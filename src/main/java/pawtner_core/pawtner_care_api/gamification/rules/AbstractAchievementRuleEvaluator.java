package pawtner_core.pawtner_care_api.gamification.rules;

import java.util.Map;

import pawtner_core.pawtner_care_api.gamification.entity.UserStats;

abstract class AbstractAchievementRuleEvaluator implements AchievementRuleEvaluator {

    protected long resolveTarget(Map<String, Object> config, long defaultValue) {
        Object target = config.get("target");
        if (target == null) {
            return defaultValue;
        }

        return Long.parseLong(String.valueOf(target));
    }

    protected String resolveStatField(Map<String, Object> config, String defaultField) {
        Object statField = config.get("statField");
        return statField == null ? defaultField : String.valueOf(statField);
    }

    protected boolean resolveBooleanStat(UserStats userStats, String fieldName) {
        if ("isRegistered".equals(fieldName)) {
            return Boolean.TRUE.equals(userStats.getIsRegistered());
        }

        throw new IllegalArgumentException("Unsupported boolean statField: " + fieldName);
    }

    protected long resolveNumericStat(UserStats userStats, String fieldName) {
        return switch (fieldName) {
            case "totalAdoptedPets" -> userStats.getTotalAdoptedPets();
            case "totalDonations" -> userStats.getTotalDonations();
            case "monthsActive" -> userStats.getMonthsActive();
            default -> throw new IllegalArgumentException("Unsupported numeric statField: " + fieldName);
        };
    }
}
