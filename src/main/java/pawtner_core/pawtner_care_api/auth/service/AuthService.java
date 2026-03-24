package pawtner_core.pawtner_care_api.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.auth.dto.AuthResponse;
import pawtner_core.pawtner_care_api.auth.dto.LoginRequest;
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
    private final String apiBearerToken;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
        UserService userService,
        UserRepository userRepository,
        @Value("${API_BEARER_TOKEN}") String apiBearerToken,
        PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.apiBearerToken = apiBearerToken;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse signup(SignupRequest request) {
        UserRequest userRequest = new UserRequest(
            request.firstName(),
            request.middleName(),
            request.lastName(),
            request.email(),
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

        if (!isPasswordValid(user, request.password())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return new AuthResponse(
            "Bearer",
            apiBearerToken,
            new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
            )
        );
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
}

