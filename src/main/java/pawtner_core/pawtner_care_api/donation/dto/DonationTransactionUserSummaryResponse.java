package pawtner_core.pawtner_care_api.donation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import pawtner_core.pawtner_care_api.user.enums.UserRole;

public record DonationTransactionUserSummaryResponse(
    UUID id,
    String firstName,
    String middleName,
    String lastName,
    String fullName,
    String email,
    String profilePicture,
    UserRole role,
    Boolean active,
    LocalDateTime createdDate,
    LocalDateTime updatedDate
) {
}
