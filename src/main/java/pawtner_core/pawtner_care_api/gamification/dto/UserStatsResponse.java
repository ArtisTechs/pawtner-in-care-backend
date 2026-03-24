package pawtner_core.pawtner_care_api.gamification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserStatsResponse(
    UUID userId,
    Long totalAdoptedPets,
    Long totalDonations,
    Long monthsActive,
    Boolean isRegistered,
    LocalDateTime updatedAt
) {
}
