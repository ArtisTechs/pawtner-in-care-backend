package pawtner_core.pawtner_care_api.gamification.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.gamification.entity.ActivityEvent;
import pawtner_core.pawtner_care_api.gamification.entity.UserStats;
import pawtner_core.pawtner_care_api.gamification.enums.ActivityEventType;
import pawtner_core.pawtner_care_api.gamification.repository.UserStatsRepository;

@Service
public class UserStatsService {

    private final UserStatsRepository userStatsRepository;

    public UserStatsService(UserStatsRepository userStatsRepository) {
        this.userStatsRepository = userStatsRepository;
    }

    @Transactional
    public UserStats applyEvent(ActivityEvent activityEvent) {
        UserStats userStats = getOrCreate(activityEvent.getUserId());
        long increment = resolveIncrement(activityEvent.getValue());

        ActivityEventType eventType = activityEvent.getEventType();
        if (eventType == ActivityEventType.USER_REGISTERED) {
            userStats.setIsRegistered(Boolean.TRUE);
        } else if (eventType == ActivityEventType.PET_ADOPTED) {
            userStats.setTotalAdoptedPets(userStats.getTotalAdoptedPets() + increment);
        } else if (eventType == ActivityEventType.DONATION_MADE) {
            userStats.setTotalDonations(userStats.getTotalDonations() + increment);
        } else if (eventType == ActivityEventType.USER_ACTIVE_MONTH) {
            userStats.setMonthsActive(userStats.getMonthsActive() + increment);
        }

        return userStatsRepository.save(userStats);
    }

    @Transactional
    public UserStats getOrCreate(UUID userId) {
        return userStatsRepository.findById(userId).orElseGet(() -> {
            UserStats userStats = new UserStats();
            userStats.setUserId(userId);
            userStats.setTotalAdoptedPets(0L);
            userStats.setTotalDonations(0L);
            userStats.setMonthsActive(0L);
            userStats.setIsRegistered(Boolean.FALSE);
            return userStatsRepository.save(userStats);
        });
    }

    private long resolveIncrement(BigDecimal value) {
        if (value == null) {
            return 1L;
        }

        long increment = value.longValue();
        return increment <= 0 ? 1L : increment;
    }
}

