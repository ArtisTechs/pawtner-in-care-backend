package pawtner_core.pawtner_care_api.emergency.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pawtner_core.pawtner_care_api.emergency.dto.EmergencySosRequest;
import pawtner_core.pawtner_care_api.emergency.dto.EmergencySosResponse;
import pawtner_core.pawtner_care_api.emergency.entity.EmergencySos;
import pawtner_core.pawtner_care_api.emergency.enums.EmergencySosStatus;
import pawtner_core.pawtner_care_api.emergency.enums.EmergencySosType;
import pawtner_core.pawtner_care_api.emergency.repository.EmergencySosRepository;
import pawtner_core.pawtner_care_api.user.entity.User;
import pawtner_core.pawtner_care_api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmergencySosServiceTest {

    @Mock
    private EmergencySosRepository emergencySosRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmergencySosService emergencySosService;

    @Test
    void createEmergencySosRejectsFourthRequestForTheDay() {
        UUID userId = UUID.randomUUID();
        EmergencySosRequest request = createRequest(userId);

        when(emergencySosRepository.countByPersonFilledIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            any(UUID.class),
            any(java.time.LocalDateTime.class),
            any(java.time.LocalDateTime.class)
        )).thenReturn(3L);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> emergencySosService.createEmergencySos(request)
        );

        assertEquals("You can only create up to 3 SOS requests per day", exception.getMessage());
        verify(emergencySosRepository, never()).save(any(EmergencySos.class));
    }

    @Test
    void createEmergencySosAllowsThirdRequestOrBelow() {
        UUID userId = UUID.randomUUID();
        User user = createUser(userId);
        EmergencySosRequest request = createRequest(userId);

        when(emergencySosRepository.countByPersonFilledIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            any(UUID.class),
            any(java.time.LocalDateTime.class),
            any(java.time.LocalDateTime.class)
        )).thenReturn(2L);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(emergencySosRepository.save(any(EmergencySos.class))).thenAnswer(invocation -> {
            EmergencySos emergencySos = invocation.getArgument(0);
            emergencySos.setId(UUID.randomUUID());
            return emergencySos;
        });

        EmergencySosResponse response = emergencySosService.createEmergencySos(request);

        assertEquals(userId, response.personFilledId());
        assertEquals(EmergencySosType.INJURED, response.type());
        verify(emergencySosRepository).save(any(EmergencySos.class));
    }

    private EmergencySosRequest createRequest(UUID userId) {
        return new EmergencySosRequest(
            userId,
            EmergencySosType.INJURED,
            "123 Sample Street",
            new BigDecimal("14.5995000"),
            new BigDecimal("120.9842000"),
            "Near the main gate",
            "Need immediate help",
            EmergencySosStatus.REQUESTED
        );
    }

    private User createUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane@example.com");
        return user;
    }
}
