package pawtner_core.pawtner_care_api.gamification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.gamification.dto.ActivityEventRequest;
import pawtner_core.pawtner_care_api.gamification.dto.ActivityEventResponse;
import pawtner_core.pawtner_care_api.gamification.dto.UserAchievementResponse;
import pawtner_core.pawtner_care_api.gamification.entity.ActivityEvent;
import pawtner_core.pawtner_care_api.gamification.entity.UserAchievement;
import pawtner_core.pawtner_care_api.gamification.entity.UserStats;
import pawtner_core.pawtner_care_api.gamification.repository.ActivityEventRepository;

@Service
public class ActivityEventService {

    private final ActivityEventRepository activityEventRepository;
    private final UserStatsService userStatsService;
    private final AchievementEvaluationService achievementEvaluationService;
    private final AchievementMapperService achievementMapperService;

    public ActivityEventService(
        ActivityEventRepository activityEventRepository,
        UserStatsService userStatsService,
        AchievementEvaluationService achievementEvaluationService,
        AchievementMapperService achievementMapperService
    ) {
        this.activityEventRepository = activityEventRepository;
        this.userStatsService = userStatsService;
        this.achievementEvaluationService = achievementEvaluationService;
        this.achievementMapperService = achievementMapperService;
    }

    @Transactional
    public RecordedActivityEventResult recordEventAndUnlocks(ActivityEventRequest request) {
        ActivityEvent activityEvent = new ActivityEvent();
        activityEvent.setUserId(request.userId());
        activityEvent.setEventType(request.eventType());
        activityEvent.setValue(request.value());
        activityEvent.setMetadata(request.metadata());

        ActivityEvent savedEvent = activityEventRepository.save(activityEvent);
        UserStats userStats = userStatsService.applyEvent(savedEvent);
        List<UserAchievement> unlockedAchievements = achievementEvaluationService.evaluateAutoAchievements(
            request.userId(),
            userStats,
            savedEvent
        );

        List<UserAchievementResponse> unlockedResponses = unlockedAchievements.stream()
            .map(achievementMapperService::toUserAchievementResponse)
            .toList();

        return new RecordedActivityEventResult(
            achievementMapperService.toActivityEventResponse(savedEvent),
            unlockedResponses
        );
    }

    @Transactional
    public ActivityEventResponse recordEvent(ActivityEventRequest request) {
        return recordEventAndUnlocks(request).activityEvent();
    }

    public record RecordedActivityEventResult(
        ActivityEventResponse activityEvent,
        List<UserAchievementResponse> unlockedAchievements
    ) {
    }
}
