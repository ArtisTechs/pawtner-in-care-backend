package pawtner_core.pawtner_care_api.dto;

import java.util.UUID;

public record PetAdopterResponse(
    UUID id,
    String firstName,
    String middleName,
    String lastName,
    String email
) {
}
