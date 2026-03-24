package pawtner_core.pawtner_care_api.donation.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DonationTransactionRequest(
    @NotNull(message = "User is required")
    UUID userId,

    @NotNull(message = "Payment mode is required")
    UUID paymentModeId,

    @NotNull(message = "Donation campaign is required")
    UUID donationCampaignId,

    @NotBlank(message = "Photo proof of donation is required")
    @Size(max = 500, message = "Photo proof of donation must not exceed 500 characters")
    String photoProof,

    @NotNull(message = "Donated amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Donated amount must be greater than 0")
    BigDecimal donatedAmount,

    @Size(max = 5000, message = "Message must not exceed 5000 characters")
    String message
) {
}

