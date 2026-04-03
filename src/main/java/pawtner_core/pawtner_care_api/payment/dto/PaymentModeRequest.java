package pawtner_core.pawtner_care_api.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentModeRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name,

    @Size(max = 100, message = "Account number must not exceed 100 characters")
    String accountNumber,

    @Size(max = 500, message = "Photo QR must not exceed 500 characters")
    String photoQr
) {
}

