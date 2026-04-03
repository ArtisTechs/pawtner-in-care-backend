package pawtner_core.pawtner_care_api.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pawtner_core.pawtner_care_api.auth.entity.AuthToken;
import pawtner_core.pawtner_care_api.auth.repository.AuthTokenRepository;
import pawtner_core.pawtner_care_api.user.entity.User;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    @Mock
    private AuthTokenRepository authTokenRepository;

    private AuthTokenService authTokenService;

    @BeforeEach
    void setUp() {
        authTokenService = new AuthTokenService(authTokenRepository, 90);
    }

    @Test
    void issueTokenUsesProductionStyleOpaqueTokenFormatAndNinetyDayExpiry() {
        User user = createUser();
        ArgumentCaptor<AuthToken> authTokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        Instant beforeIssue = Instant.now();

        when(authTokenRepository.save(authTokenCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        String rawToken = authTokenService.issueToken(user);

        Instant afterIssue = Instant.now();
        AuthToken savedToken = authTokenCaptor.getValue();

        assertNotNull(rawToken);
        assertTrue(rawToken.matches("^pat_[A-Za-z0-9_-]{43}$"));
        assertNotNull(savedToken.getTokenHash());
        assertEquals(64, savedToken.getTokenHash().length());

        Instant minExpectedExpiry = beforeIssue.plus(Duration.ofDays(90));
        Instant maxExpectedExpiry = afterIssue.plus(Duration.ofDays(90));
        assertFalse(savedToken.getExpiresAt().isBefore(minExpectedExpiry));
        assertFalse(savedToken.getExpiresAt().isAfter(maxExpectedExpiry));
    }

    @Test
    void extractBearerTokenRejectsMalformedHeaders() {
        IllegalArgumentException missingPrefix = assertThrows(
            IllegalArgumentException.class,
            () -> authTokenService.extractBearerToken("Token pat_invalid")
        );
        assertEquals("Missing or invalid Authorization header", missingPrefix.getMessage());

        IllegalArgumentException malformedToken = assertThrows(
            IllegalArgumentException.class,
            () -> authTokenService.extractBearerToken("Bearer pat_invalid")
        );
        assertEquals("Invalid bearer token", malformedToken.getMessage());
    }

    @Test
    void isValidShortCircuitsMalformedTokens() {
        assertFalse(authTokenService.isValid("not-a-production-token"));
    }

    @Test
    void getUserIdFromTokenReturnsUserForValidStoredToken() {
        User user = createUser();
        String rawToken = authTokenService.issueToken(user);
        when(authTokenRepository.findByTokenHashAndExpiresAtAfter(any(String.class), any(Instant.class)))
            .thenReturn(Optional.of(createStoredToken(user)));

        UUID userId = authTokenService.getUserIdFromToken(rawToken);

        assertEquals(user.getId(), userId);
    }

    private User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane@example.com");
        return user;
    }

    private AuthToken createStoredToken(User user) {
        AuthToken authToken = new AuthToken();
        authToken.setId(UUID.randomUUID());
        authToken.setUser(user);
        authToken.setTokenHash("a".repeat(64));
        authToken.setCreatedAt(Instant.now());
        authToken.setExpiresAt(Instant.now().plus(Duration.ofDays(90)));
        return authToken;
    }
}
