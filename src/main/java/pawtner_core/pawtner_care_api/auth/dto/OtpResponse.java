package pawtner_core.pawtner_care_api.auth.dto;

import java.time.Instant;

public record OtpResponse(
    String email,
    String purpose,
    String message,
    Instant expiresAt
) {
}

