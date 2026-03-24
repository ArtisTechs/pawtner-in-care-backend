package pawtner_core.pawtner_care_api.gamification.rules;

public record RuleProgress(
    long current,
    long target,
    boolean unlocked
) {
}

