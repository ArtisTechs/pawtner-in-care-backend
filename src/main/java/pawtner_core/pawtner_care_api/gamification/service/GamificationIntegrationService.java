package pawtner_core.pawtner_care_api.gamification.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import pawtner_core.pawtner_care_api.gamification.dto.ActivityEventRequest;
import pawtner_core.pawtner_care_api.gamification.enums.ActivityEventType;

@Service
public class GamificationIntegrationService {

    private final ActivityEventService activityEventService;

    public GamificationIntegrationService(ActivityEventService activityEventService) {
        this.activityEventService = activityEventService;
    }

    public ActivityEventService.RecordedActivityEventResult onUserRegistered(UUID userId) {
        return activityEventService.recordEventAndUnlocks(
            new ActivityEventRequest(userId, ActivityEventType.USER_REGISTERED, BigDecimal.ONE, null)
        );
    }

    public ActivityEventService.RecordedActivityEventResult onPetAdopted(UUID userId) {
        return activityEventService.recordEventAndUnlocks(
            new ActivityEventRequest(userId, ActivityEventType.PET_ADOPTED, BigDecimal.ONE, null)
        );
    }

    public ActivityEventService.RecordedActivityEventResult onDonationMade(UUID userId, BigDecimal donationCountIncrement) {
        BigDecimal increment = donationCountIncrement == null ? BigDecimal.ONE : donationCountIncrement;
        return activityEventService.recordEventAndUnlocks(
            new ActivityEventRequest(userId, ActivityEventType.DONATION_MADE, increment, null)
        );
    }

    public ActivityEventService.RecordedActivityEventResult onUserActiveMonth(UUID userId) {
        return activityEventService.recordEventAndUnlocks(
            new ActivityEventRequest(userId, ActivityEventType.USER_ACTIVE_MONTH, BigDecimal.ONE, null)
        );
    }
}

