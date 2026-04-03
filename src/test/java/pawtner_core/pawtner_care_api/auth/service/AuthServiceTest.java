package pawtner_core.pawtner_care_api.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import pawtner_core.pawtner_care_api.auth.dto.AuthResponse;
import pawtner_core.pawtner_care_api.auth.dto.LoginRequest;
import pawtner_core.pawtner_care_api.user.entity.User;
import pawtner_core.pawtner_care_api.user.enums.UserRole;
import pawtner_core.pawtner_care_api.user.repository.UserRepository;
import pawtner_core.pawtner_care_api.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsUniqueIssuedTokenInsteadOfStoredPasswordOrUserId() {
        User user = createUser("jane@example.com", "$2a$10$hashed-password-value");
        String issuedToken = "fresh-login-token";

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Test1234!", user.getPassword())).thenReturn(true);
        when(authTokenService.issueToken(user)).thenReturn(issuedToken);

        AuthResponse response = authService.login(new LoginRequest("Jane@Example.com ", "Test1234!"));

        assertEquals("Bearer", response.tokenType());
        assertEquals(issuedToken, response.accessToken());
        assertNotEquals(user.getId().toString(), response.accessToken());
        assertNotEquals(user.getPassword(), response.accessToken());
        assertEquals(user.getEmail(), response.user().email());
        assertEquals(user.getActive(), response.user().active());
        assertEquals(user.getCreatedDate(), response.user().createdDate());
        assertEquals(user.getUpdatedDate(), response.user().updatedDate());
        verify(authTokenService).issueToken(user);
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = createUser("jane@example.com", "$2a$10$hashed-password-value");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPassword())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(new LoginRequest("jane@example.com", "wrong-password"))
        );

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void loginMigratesLegacyPlaintextPasswordsAndStillReturnsIssuedToken() {
        User user = createUser("jane@example.com", "plaintext-password");
        String encodedPassword = "encoded-password";
        String issuedToken = "new-token";

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("plaintext-password")).thenReturn(encodedPassword);
        when(authTokenService.issueToken(any(User.class))).thenReturn(issuedToken);

        AuthResponse response = authService.login(new LoginRequest("jane@example.com", "plaintext-password"));

        assertEquals(issuedToken, response.accessToken());
        assertEquals(encodedPassword, user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void loginRejectsInactiveUsers() {
        User user = createUser("jane@example.com", "$2a$10$hashed-password-value");
        user.setActive(false);

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(new LoginRequest("jane@example.com", "Test1234!"))
        );

        assertEquals("User account is inactive", exception.getMessage());
    }

    private User createUser(String email, String password) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setCreatedDate(now);
        user.setUpdatedDate(now);
        return user;
    }

    @Test
    void userTimestampsAreInitializedAndUpdatedAutomatically() {
        User user = new User();

        user.applyDefaults();

        assertNotNull(user.getCreatedDate());
        assertNotNull(user.getUpdatedDate());
        assertEquals(Boolean.TRUE, user.getActive());
        assertEquals(user.getCreatedDate(), user.getUpdatedDate());

        LocalDateTime createdDate = user.getCreatedDate();
        user.setUpdatedDate(createdDate.minusSeconds(5));

        user.updateTimestamp();

        assertEquals(createdDate, user.getCreatedDate());
        assertNotNull(user.getUpdatedDate());
    }

    @Test
    void adminUsersDefaultToInactive() {
        User user = new User();
        user.setRole(UserRole.ADMIN);

        user.applyDefaults();

        assertEquals(Boolean.FALSE, user.getActive());
    }
}
