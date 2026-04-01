package pawtner_core.pawtner_care_api.pet.dto;

import java.util.UUID;

public record AdoptionRequestUserResponse(
    UUID id,
    String firstName,
    String middleName,
    String lastName,
    String email,
    String profilePicture
) {
}
