package pawtner_core.pawtner_care_api.gamification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pawtner_core.pawtner_care_api.gamification.dto.ActivityEventRequest;
import pawtner_core.pawtner_care_api.gamification.enums.ActivityEventType;
import pawtner_core.pawtner_care_api.gamification.repository.ActivityEventRepository;
import pawtner_core.pawtner_care_api.user.entity.User;
import pawtner_core.pawtner_care_api.user.enums.UserRole;
import pawtner_core.pawtner_care_api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ActivityEventServiceTest {

    @Mock
    private ActivityEventRepository activityEventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatsService userStatsService;

    @Mock
    private AchievementEvaluationService achievementEvaluationService;

    @Mock
    private AchievementMapperService achievementMapperService;

    @InjectMocks
    private ActivityEventService activityEventService;

    @Test
    void recordEventAndUnlocksRejectsAdminUsers() {
        UUID userId = UUID.randomUUID();
        User adminUser = new User();
        adminUser.setId(userId);
        adminUser.setRole(UserRole.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> activityEventService.recordEventAndUnlocks(
                new ActivityEventRequest(userId, ActivityEventType.USER_REGISTERED, BigDecimal.ONE, null)
            )
        );

        assertEquals("Gamification auto-apply is only available for non-admin users", exception.getMessage());
        verify(activityEventRepository, never()).save(any());
        verify(userStatsService, never()).applyEvent(any());
        verify(achievementEvaluationService, never()).evaluateAutoAchievements(any(), any(), any());
    }
}
