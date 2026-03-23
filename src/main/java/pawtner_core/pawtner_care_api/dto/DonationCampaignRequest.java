package pawtner_core.pawtner_care_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pawtner_core.pawtner_care_api.enums.DonationCampaignStatus;
import pawtner_core.pawtner_care_api.enums.DonationCampaignType;

public record DonationCampaignRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    String title,

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description,

    @NotNull(message = "Total cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total cost must be greater than 0")
    BigDecimal totalCost,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate deadline,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate,

    @Size(max = 500, message = "Photo must not exceed 500 characters")
    String photo,

    Boolean isUrgent,

    @NotNull(message = "Status is required")
    DonationCampaignStatus status,

    @NotNull(message = "Type is required")
    DonationCampaignType type
) {
}
