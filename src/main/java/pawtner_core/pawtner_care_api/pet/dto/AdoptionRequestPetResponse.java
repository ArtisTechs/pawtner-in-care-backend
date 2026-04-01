package pawtner_core.pawtner_care_api.pet.dto;

import java.util.UUID;
import pawtner_core.pawtner_care_api.pet.enums.PetStatus;

public record AdoptionRequestPetResponse(
    UUID id,
    String name,
    String type,
    String race,
    PetStatus status,
    String photo
) {
}
