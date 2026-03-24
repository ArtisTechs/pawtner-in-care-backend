package pawtner_core.pawtner_care_api.donation.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DonationTransactionResponse(
    UUID id,
    UUID userId,
    String userFullName,
    String userEmail,
    UUID paymentModeId,
    String paymentModeName,
    UUID donationCampaignId,
    String donationCampaignTitle,
    String photoProof,
    BigDecimal donatedAmount,
    String message
) {
}

