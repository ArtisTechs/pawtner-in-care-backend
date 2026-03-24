package pawtner_core.pawtner_care_api.gamification.service;

import org.springframework.stereotype.Service;

import pawtner_core.pawtner_care_api.gamification.dto.AchievementResponse;
import pawtner_core.pawtner_care_api.gamification.dto.ActivityEventResponse;
import pawtner_core.pawtner_care_api.gamification.dto.UserAchievementResponse;
import pawtner_core.pawtner_care_api.gamification.dto.UserStatsResponse;
import pawtner_core.pawtner_care_api.gamification.entity.Achievement;
import pawtner_core.pawtner_care_api.gamification.entity.ActivityEvent;
import pawtner_core.pawtner_care_api.gamification.entity.UserAchievement;
import pawtner_core.pawtner_care_api.gamification.entity.UserStats;

@Service
public class AchievementMapperService {

    public AchievementResponse toAchievementResponse(Achievement achievement) {
        return new AchievementResponse(
            achievement.getId(),
            achievement.getCode(),
            achievement.getTitle(),
            achievement.getDescription(),
            achievement.getIconUrl(),
            achievement.getCategory(),
            achievement.getPoints(),
            achievement.getRarity(),
            achievement.getIsActive(),
            achievement.getIsRepeatable(),
            achievement.getVisibility(),
            achievement.getAssignmentType(),
            achievement.getTriggerType(),
            achievement.getRuleType(),
            achievement.getRuleConfig(),
            achievement.getStartAt(),
            achievement.getEndAt(),
            achievement.getCreatedAt(),
            achievement.getUpdatedAt()
        );
    }

    public UserAchievementResponse toUserAchievementResponse(UserAchievement userAchievement) {
        return new UserAchievementResponse(
            userAchievement.getId(),
            userAchievement.getUserId(),
            toAchievementResponse(userAchievement.getAchievement()),
            userAchievement.getProgressCurrent(),
            userAchievement.getProgressTarget(),
            userAchievement.getIsUnlocked(),
            userAchievement.getUnlockedAt(),
            userAchievement.getSourceEvent(),
            userAchievement.getMetadata(),
            userAchievement.getCreatedAt(),
            userAchievement.getUpdatedAt()
        );
    }

    public UserStatsResponse toUserStatsResponse(UserStats userStats) {
        return new UserStatsResponse(
            userStats.getUserId(),
            userStats.getTotalAdoptedPets(),
            userStats.getTotalDonations(),
            userStats.getMonthsActive(),
            userStats.getIsRegistered(),
            userStats.getUpdatedAt()
        );
    }

    public ActivityEventResponse toActivityEventResponse(ActivityEvent activityEvent) {
        return new ActivityEventResponse(
            activityEvent.getId(),
            activityEvent.getUserId(),
            activityEvent.getEventType(),
            activityEvent.getValue(),
            activityEvent.getMetadata(),
            activityEvent.getCreatedAt()
        );
    }
}
