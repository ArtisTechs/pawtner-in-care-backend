package pawtner_core.pawtner_care_api.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import pawtner_core.pawtner_care_api.user.enums.UserRole;

public record UserResponse(
    UUID id,
    String firstName,
    String middleName,
    String lastName,
    String email,
    String profilePicture,
    UserRole role,
    Boolean active,
    LocalDateTime createdDate,
    LocalDateTime updatedDate
) {
}

