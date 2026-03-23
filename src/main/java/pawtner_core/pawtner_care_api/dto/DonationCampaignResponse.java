package pawtner_core.pawtner_care_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import pawtner_core.pawtner_care_api.enums.DonationCampaignStatus;
import pawtner_core.pawtner_care_api.enums.DonationCampaignType;

public record DonationCampaignResponse(
    UUID id,
    String title,
    String description,
    BigDecimal totalCost,
    LocalDate deadline,
    LocalDate startDate,
    LocalDateTime updatedDate,
    String photo,
    Boolean isUrgent,
    DonationCampaignStatus status,
    DonationCampaignType type
) {
}
