package pawtner_core.pawtner_care_api.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.auth.dto.AuthResponse;
import pawtner_core.pawtner_care_api.auth.dto.LoginRequest;
import pawtner_core.pawtner_care_api.auth.dto.ResetPasswordRequest;
import pawtner_core.pawtner_care_api.auth.dto.ResetPasswordResponse;
import pawtner_core.pawtner_care_api.auth.dto.SignupRequest;
import pawtner_core.pawtner_care_api.user.dto.UserRequest;
import pawtner_core.pawtner_care_api.user.dto.UserResponse;
import pawtner_core.pawtner_care_api.user.entity.User;
import pawtner_core.pawtner_care_api.user.enums.UserRole;
import pawtner_core.pawtner_care_api.user.repository.UserRepository;
import pawtner_core.pawtner_care_api.user.service.UserService;

@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final AuthTokenService authTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
        UserService userService,
        UserRepository userRepository,
        OtpService otpService,
        AuthTokenService authTokenService,
        PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.authTokenService = authTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse signup(SignupRequest request) {
        otpService.consumeVerifiedSignupOtp(request.email());

        UserRequest userRequest = new UserRequest(
            request.firstName(),
            request.middleName(),
            request.lastName(),
            request.email(),
            request.profilePicture(),
            request.password(),
            UserRole.USER
        );

        return userService.createUser(userRequest);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalArgumentException("User account is inactive");
        }

        if (!isPasswordValid(user, request.password())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String accessToken = authTokenService.issueToken(user);

        return new AuthResponse(
            "Bearer",
            accessToken,
            new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfilePicture(),
                user.getRole(),
                user.getActive(),
                user.getCreatedDate(),
                user.getUpdatedDate()
            )
        );
    }

    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        validatePasswordConfirmation(request.newPassword(), request.confirmPassword());

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new IllegalArgumentException("User with email " + normalizedEmail + " was not found"));

        otpService.consumeResetPasswordOtp(normalizedEmail, request.otp());

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return new ResetPasswordResponse(normalizedEmail, "Password reset successfully");
    }

    private boolean isPasswordValid(User user, String rawPassword) {
        String storedPassword = user.getPassword();

        if (storedPassword != null && storedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (!rawPassword.equals(storedPassword)) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        return true;
    }

    private void validatePasswordConfirmation(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
    }
}

