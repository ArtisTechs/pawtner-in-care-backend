package pawtner_core.pawtner_care_api.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.user.entity.User;
import pawtner_core.pawtner_care_api.user.enums.UserRole;
import pawtner_core.pawtner_care_api.user.repository.UserRepository;

@Service
public class MainAdminBootstrapService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String email;
    private final String password;

    public MainAdminBootstrapService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.main-admin.first-name:}") String firstName,
        @Value("${app.main-admin.middle-name:}") String middleName,
        @Value("${app.main-admin.last-name:}") String lastName,
        @Value("${app.main-admin.email:}") String email,
        @Value("${app.main-admin.password:}") String password
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createMainAdminIfConfigured() {
        if (!isConfigured()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        User user = new User();
        user.setFirstName(firstName.trim());
        user.setMiddleName(normalizeOptionalText(middleName));
        user.setLastName(lastName.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password.trim()));
        user.setRole(UserRole.ADMIN);
        user.setActive(true);

        userRepository.save(user);
    }

    private boolean isConfigured() {
        return hasText(firstName)
            && hasText(lastName)
            && hasText(email)
            && hasText(password);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeOptionalText(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
