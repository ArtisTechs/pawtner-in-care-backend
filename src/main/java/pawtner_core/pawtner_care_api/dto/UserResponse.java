package pawtner_core.pawtner_care_api.dto;

import java.util.UUID;

import pawtner_core.pawtner_care_api.enums.UserRole;

public record UserResponse(
    UUID id,
    String firstName,
    String middleName,
    String lastName,
    String email,
    UserRole role
) {
}
