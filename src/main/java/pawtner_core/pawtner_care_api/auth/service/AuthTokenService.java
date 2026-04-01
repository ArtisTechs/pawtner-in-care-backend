package pawtner_core.pawtner_care_api.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.auth.entity.AuthToken;
import pawtner_core.pawtner_care_api.auth.repository.AuthTokenRepository;
import pawtner_core.pawtner_care_api.user.entity.User;

@Service
public class AuthTokenService {

    private static final int TOKEN_BYTES = 32;

    private final AuthTokenRepository authTokenRepository;
    private final Duration tokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthTokenService(
        AuthTokenRepository authTokenRepository,
        @Value("${app.auth.token-ttl-hours:24}") long tokenTtlHours
    ) {
        this.authTokenRepository = authTokenRepository;
        this.tokenTtl = Duration.ofHours(tokenTtlHours);
    }

    @Transactional
    public String issueToken(User user) {
        String rawToken = generateTokenValue();

        AuthToken authToken = new AuthToken();
        authToken.setUser(user);
        authToken.setTokenHash(hashToken(rawToken));
        authToken.setExpiresAt(Instant.now().plus(tokenTtl));

        authTokenRepository.save(authToken);
        return rawToken;
    }

    @Transactional(readOnly = true)
    public boolean isValid(String rawToken) {
        return authTokenRepository.findByTokenHashAndExpiresAtAfter(hashToken(rawToken), Instant.now()).isPresent();
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
