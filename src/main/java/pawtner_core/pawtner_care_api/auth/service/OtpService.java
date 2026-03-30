package pawtner_core.pawtner_care_api.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.auth.dto.OtpConfirmRequest;
import pawtner_core.pawtner_care_api.auth.dto.OtpResponse;
import pawtner_core.pawtner_care_api.auth.dto.OtpSendRequest;
import pawtner_core.pawtner_care_api.auth.entity.EmailOtp;
import pawtner_core.pawtner_care_api.auth.repository.EmailOtpRepository;
import pawtner_core.pawtner_care_api.user.repository.UserRepository;

@Service
public class OtpService {

    private static final Set<String> ALLOWED_PURPOSES = Set.of("signup", "login", "reset-password");

    private final EmailOtpRepository emailOtpRepository;
    private final BrevoEmailService brevoEmailService;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom;
    private final long expiryMinutes;

    public OtpService(
        EmailOtpRepository emailOtpRepository,
        BrevoEmailService brevoEmailService,
        UserRepository userRepository,
        @Value("${OTP_EXPIRY_MINUTES:10}") long expiryMinutes
    ) {
        this.emailOtpRepository = emailOtpRepository;
        this.brevoEmailService = brevoEmailService;
        this.userRepository = userRepository;
        this.secureRandom = new SecureRandom();
        this.expiryMinutes = expiryMinutes;
    }

    @Transactional
    public OtpResponse sendOtp(OtpSendRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String normalizedPurpose = normalizePurpose(request.purpose());
        validateSendOtpRequest(normalizedEmail, normalizedPurpose);
        String otpCode = generateOtpCode();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiryMinutes, ChronoUnit.MINUTES);

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(normalizedEmail);
        emailOtp.setPurpose(normalizedPurpose);
        emailOtp.setOtpHash(hashOtp(otpCode));
        emailOtp.setCreatedAt(now);
        emailOtp.setExpiresAt(expiresAt);

        emailOtpRepository.save(emailOtp);
        brevoEmailService.sendOtpEmail(normalizedEmail, normalizedPurpose, otpCode, expiryMinutes);

        return new OtpResponse(
            normalizedEmail,
            normalizedPurpose,
            "OTP sent successfully",
            expiresAt
        );
    }

    private void validateSendOtpRequest(String email, String purpose) {
        validateEmailAvailabilityForSignup(email, purpose);

        if ("reset-password".equals(purpose) && !userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is not registered");
        }
    }

    @Transactional
    public OtpResponse confirmOtp(OtpConfirmRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String normalizedPurpose = normalizePurpose(request.purpose());
        validateEmailAvailabilityForSignup(normalizedEmail, normalizedPurpose);
        EmailOtp emailOtp = findLatestOtp(normalizedEmail, normalizedPurpose);
        validateOtpCanBeUsed(emailOtp);
        validateOtpCode(emailOtp, request.otp());

        emailOtp.setVerifiedAt(Instant.now());
        emailOtpRepository.save(emailOtp);

        return new OtpResponse(
            normalizedEmail,
            normalizedPurpose,
            "OTP confirmed successfully",
            emailOtp.getExpiresAt()
        );
    }

    @Transactional
    public void consumeVerifiedResetPasswordOtp(String email) {
        EmailOtp emailOtp = findLatestOtp(normalizeEmail(email), "reset-password");
        validateOtpCanBeUsed(emailOtp);

        if (emailOtp.getVerifiedAt() == null) {
            throw new IllegalArgumentException("Reset password OTP has not been verified yet");
        }

        emailOtp.setConsumedAt(Instant.now());
        emailOtpRepository.save(emailOtp);
    }

    @Transactional
    public void consumeResetPasswordOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        EmailOtp emailOtp = findLatestOtp(normalizedEmail, "reset-password");
        validateOtpCanBeUsed(emailOtp);
        validateOtpCode(emailOtp, otp);

        if (emailOtp.getVerifiedAt() == null) {
            throw new IllegalArgumentException("Reset password OTP has not been verified yet");
        }

        emailOtp.setConsumedAt(Instant.now());
        emailOtpRepository.save(emailOtp);
    }

    @Transactional
    public void consumeVerifiedSignupOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        validateEmailAvailabilityForSignup(normalizedEmail, "signup");

        EmailOtp emailOtp = findLatestOtp(normalizedEmail, "signup");
        validateOtpCanBeUsed(emailOtp);

        if (emailOtp.getVerifiedAt() == null) {
            throw new IllegalArgumentException("Signup OTP has not been verified yet");
        }

        emailOtp.setConsumedAt(Instant.now());
        emailOtpRepository.save(emailOtp);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePurpose(String purpose) {
        String normalizedPurpose = purpose.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_PURPOSES.contains(normalizedPurpose)) {
            throw new IllegalArgumentException("Unsupported OTP purpose: " + normalizedPurpose);
        }
        return normalizedPurpose;
    }

    private void validateEmailAvailabilityForSignup(String email, String purpose) {
        if ("signup".equals(purpose) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
    }

    private EmailOtp findLatestOtp(String email, String purpose) {
        return emailOtpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
            .orElseThrow(() -> new IllegalArgumentException("No OTP request found for this email and purpose"));
    }

    private void validateOtpCanBeUsed(EmailOtp emailOtp) {
        if (emailOtp.getConsumedAt() != null) {
            throw new IllegalArgumentException("OTP has already been used");
        }

        if (emailOtp.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }
    }

    private void validateOtpCode(EmailOtp emailOtp, String otp) {
        if (!emailOtp.getOtpHash().equals(hashOtp(otp.trim()))) {
            throw new IllegalArgumentException("Invalid OTP");
        }
    }

    private String generateOtpCode() {
        int otp = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    private String hashOtp(String otp) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(otp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}

