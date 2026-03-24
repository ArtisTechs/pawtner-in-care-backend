package pawtner_core.pawtner_care_api.gamification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.gamification.entity.Achievement;
import pawtner_core.pawtner_care_api.gamification.entity.ActivityEvent;
import pawtner_core.pawtner_care_api.gamification.entity.UserAchievement;
import pawtner_core.pawtner_care_api.gamification.entity.UserStats;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementAssignmentType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementRuleType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementTriggerType;
import pawtner_core.pawtner_care_api.gamification.repository.AchievementRepository;
import pawtner_core.pawtner_care_api.gamification.repository.UserAchievementRepository;
import pawtner_core.pawtner_care_api.gamification.rules.AchievementRuleEvaluationContext;
import pawtner_core.pawtner_care_api.gamification.rules.AchievementRuleEvaluator;
import pawtner_core.pawtner_care_api.gamification.rules.RuleProgress;

@Service
public class AchievementEvaluationService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementMapperService achievementMapperService;
    private final Map<AchievementRuleType, AchievementRuleEvaluator> evaluators;

    public AchievementEvaluationService(
        AchievementRepository achievementRepository,
        UserAchievementRepository userAchievementRepository,
        AchievementMapperService achievementMapperService,
        List<AchievementRuleEvaluator> evaluators
    ) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.achievementMapperService = achievementMapperService;
        this.evaluators = evaluators.stream()
            .collect(Collectors.toMap(AchievementRuleEvaluator::supports, Function.identity()));
    }

    @Transactional
    public List<UserAchievement> evaluateAutoAchievements(UUID userId, UserStats userStats, ActivityEvent activityEvent) {
        AchievementTriggerType triggerType = AchievementTriggerType.valueOf(activityEvent.getEventType().name());
        List<Achievement> achievements = achievementRepository.findByIsActiveTrueAndAssignmentTypeAndTriggerType(
            AchievementAssignmentType.AUTO,
            triggerType
        );

        return achievements.stream()
            .filter(this::isWithinActiveWindow)
            .map(achievement -> evaluateAchievement(userId, achievement, userStats, activityEvent))
            .filter(userAchievement -> userAchievement != null && Boolean.TRUE.equals(userAchievement.getIsUnlocked()))
            .toList();
    }

    @Transactional
    public UserAchievement manuallyAssign(UUID userId, UUID achievementId, String sourceEvent, String metadata) {
        Achievement achievement = achievementRepository.findById(achievementId)
            .orElseThrow(() -> new ResourceNotFoundException("Achievement with id " + achievementId + " was not found"));

        if (!Boolean.TRUE.equals(achievement.getIsActive())) {
            throw new IllegalArgumentException("Achievement is not active");
        }

        if (!Boolean.TRUE.equals(achievement.getIsRepeatable())
            && userAchievementRepository.existsByUserIdAndAchievement_IdAndIsUnlockedTrue(userId, achievementId)) {
            throw new IllegalArgumentException("Achievement is already unlocked for this user");
        }

        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUserId(userId);
        userAchievement.setAchievement(achievement);
        userAchievement.setProgressCurrent(1L);
        userAchievement.setProgressTarget(1L);
        userAchievement.setIsUnlocked(Boolean.TRUE);
        userAchievement.setUnlockedAt(LocalDateTime.now());
        userAchievement.setSourceEvent(sourceEvent);
        userAchievement.setMetadata(metadata);

        return userAchievementRepository.save(userAchievement);
    }

    @Transactional(readOnly = true)
    public RuleProgress calculateProgress(Achievement achievement, UserStats userStats) {
        AchievementRuleEvaluator evaluator = evaluators.get(achievement.getRuleType());
        if (evaluator == null) {
            throw new IllegalArgumentException("No evaluator registered for ruleType " + achievement.getRuleType());
        }

        AchievementRuleEvaluationContext context = new AchievementRuleEvaluationContext(
            RuleConfigUtils.parse(achievement.getRuleConfig()),
            userStats,
            null
        );
        return evaluator.evaluate(context);
    }

    private UserAchievement evaluateAchievement(UUID userId, Achievement achievement, UserStats userStats, ActivityEvent activityEvent) {
        if (!Boolean.TRUE.equals(achievement.getIsRepeatable())
            && userAchievementRepository.existsByUserIdAndAchievement_IdAndIsUnlockedTrue(userId, achievement.getId())) {
            return null;
        }

        RuleProgress progress = calculateProgress(achievement, userStats);
        UserAchievement userAchievement = resolveUserAchievement(userId, achievement);
        userAchievement.setProgressCurrent(progress.current());
        userAchievement.setProgressTarget(progress.target());
        userAchievement.setSourceEvent(activityEvent.getEventType().name());
        userAchievement.setMetadata(activityEvent.getMetadata());

        if (progress.unlocked()) {
            userAchievement.setIsUnlocked(Boolean.TRUE);
            if (userAchievement.getUnlockedAt() == null) {
                userAchievement.setUnlockedAt(LocalDateTime.now());
            }
        }

        return userAchievementRepository.save(userAchievement);
    }

    private UserAchievement resolveUserAchievement(UUID userId, Achievement achievement) {
        if (Boolean.TRUE.equals(achievement.getIsRepeatable())) {
            UserAchievement userAchievement = new UserAchievement();
            userAchievement.setUserId(userId);
            userAchievement.setAchievement(achievement);
            return userAchievement;
        }

        return userAchievementRepository.findFirstByUserIdAndAchievement_IdOrderByCreatedAtDesc(userId, achievement.getId())
            .orElseGet(() -> {
                UserAchievement userAchievement = new UserAchievement();
                userAchievement.setUserId(userId);
                userAchievement.setAchievement(achievement);
                return userAchievement;
            });
    }

    private boolean isWithinActiveWindow(Achievement achievement) {
        LocalDateTime now = LocalDateTime.now();
        if (achievement.getStartAt() != null && now.isBefore(achievement.getStartAt())) {
            return false;
        }

        return achievement.getEndAt() == null || !now.isAfter(achievement.getEndAt());
    }
}

