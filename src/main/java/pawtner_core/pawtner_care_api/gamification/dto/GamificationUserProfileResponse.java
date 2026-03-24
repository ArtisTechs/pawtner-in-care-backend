package pawtner_core.pawtner_care_api.gamification.dto;

import java.util.UUID;

public record GamificationUserProfileResponse(
    UUID userId,
    String displayName,
    String email
) {
}

