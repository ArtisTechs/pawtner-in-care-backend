package pawtner_core.pawtner_care_api.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.auth.entity.AuthToken;
import pawtner_core.pawtner_care_api.auth.repository.AuthTokenRepository;
import pawtner_core.pawtner_care_api.user.entity.User;

@Service
public class AuthTokenService {

    private static final String TOKEN_PREFIX = "pat_";
    private static final int TOKEN_BYTES = 32;
    private static final Pattern AUTHORIZATION_HEADER_PATTERN = Pattern.compile("^Bearer (?<token>\\S+)$");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^pat_[A-Za-z0-9_-]{43}$");

    private final AuthTokenRepository authTokenRepository;
    private final Duration tokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthTokenService(
        AuthTokenRepository authTokenRepository,
        @Value("${app.auth.token-ttl-days:90}") long tokenTtlDays
    ) {
        this.authTokenRepository = authTokenRepository;
        this.tokenTtl = Duration.ofDays(tokenTtlDays);
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
        if (!isWellFormedToken(rawToken)) {
            return false;
        }

        return authTokenRepository.findByTokenHashAndExpiresAtAfter(hashToken(rawToken), Instant.now()).isPresent();
    }

    @Transactional(readOnly = true)
    public UUID getUserIdFromToken(String rawToken) {
        if (!isWellFormedToken(rawToken)) {
            throw new IllegalArgumentException("Invalid bearer token");
        }

        return authTokenRepository.findByTokenHashAndExpiresAtAfter(hashToken(rawToken), Instant.now())
            .map((token) -> token.getUser().getId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid bearer token"));
    }

    public String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        Matcher matcher = AUTHORIZATION_HEADER_PATTERN.matcher(authorizationHeader);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String rawToken = matcher.group("token");
        if (!isWellFormedToken(rawToken)) {
            throw new IllegalArgumentException("Invalid bearer token");
        }

        return rawToken;
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return TOKEN_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean isWellFormedToken(String rawToken) {
        return rawToken != null && TOKEN_PATTERN.matcher(rawToken).matches();
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
