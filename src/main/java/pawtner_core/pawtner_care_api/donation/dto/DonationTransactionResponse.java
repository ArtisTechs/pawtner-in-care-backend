package pawtner_core.pawtner_care_api.donation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DonationTransactionResponse(
    UUID id,
    String transactionId,
    DonationTransactionUserSummaryResponse user,
    DonationTransactionPaymentModeSummaryResponse paymentMode,
    DonationTransactionCampaignSummaryResponse donationCampaign,
    String photoProof,
    BigDecimal donatedAmount,
    String message,
    LocalDateTime createdDate,
    LocalDateTime updatedDate
) {
}

