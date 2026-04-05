package pawtner_core.pawtner_care_api.donation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DonationTransactionPaymentModeSummaryResponse(
    UUID id,
    String name,
    String accountNumber,
    String photoQr,
    LocalDateTime createdDate
) {
}
