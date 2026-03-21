package pawtner_core.pawtner_care_api.service;

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

import pawtner_core.pawtner_care_api.dto.OtpConfirmRequest;
import pawtner_core.pawtner_care_api.dto.OtpResponse;
import pawtner_core.pawtner_care_api.dto.OtpSendRequest;
import pawtner_core.pawtner_care_api.entity.EmailOtp;
import pawtner_core.pawtner_care_api.repository.EmailOtpRepository;

@Service
public class OtpService {

    private static final Set<String> ALLOWED_PURPOSES = Set.of("signup", "login", "reset-password");

    private final EmailOtpRepository emailOtpRepository;
    private final BrevoEmailService brevoEmailService;
    private final SecureRandom secureRandom;
    private final long expiryMinutes;

    public OtpService(
        EmailOtpRepository emailOtpRepository,
        BrevoEmailService brevoEmailService,
        @Value("${OTP_EXPIRY_MINUTES:10}") long expiryMinutes
    ) {
        this.emailOtpRepository = emailOtpRepository;
        this.brevoEmailService = brevoEmailService;
        this.secureRandom = new SecureRandom();
        this.expiryMinutes = expiryMinutes;
    }

    @Transactional
    public OtpResponse sendOtp(OtpSendRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String normalizedPurpose = normalizePurpose(request.purpose());
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

    @Transactional
    public OtpResponse confirmOtp(OtpConfirmRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String normalizedPurpose = normalizePurpose(request.purpose());
        EmailOtp emailOtp = emailOtpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(normalizedEmail, normalizedPurpose)
            .orElseThrow(() -> new IllegalArgumentException("No OTP request found for this email and purpose"));

        if (emailOtp.getConsumedAt() != null) {
            throw new IllegalArgumentException("OTP has already been used");
        }

        if (emailOtp.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }

        if (!emailOtp.getOtpHash().equals(hashOtp(request.otp().trim()))) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        Instant now = Instant.now();
        emailOtp.setVerifiedAt(now);
        emailOtp.setConsumedAt(now);
        emailOtpRepository.save(emailOtp);

        return new OtpResponse(
            normalizedEmail,
            normalizedPurpose,
            "OTP confirmed successfully",
            emailOtp.getExpiresAt()
        );
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
