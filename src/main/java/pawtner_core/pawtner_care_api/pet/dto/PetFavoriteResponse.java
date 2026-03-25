package pawtner_core.pawtner_care_api.pet.dto;

import java.util.UUID;

public record PetFavoriteResponse(
    UUID petId,
    UUID userId,
    boolean favorited
) {
}
