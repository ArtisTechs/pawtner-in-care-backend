package pawtner_core.pawtner_care_api.payment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentModeResponse(
    UUID id,
    String name,
    String photoQr,
    LocalDateTime createdDate
) {
}

