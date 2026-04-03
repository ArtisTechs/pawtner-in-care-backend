package pawtner_core.pawtner_care_api.community.dto;

import java.util.UUID;

public record CommunityUserSummaryResponse(
    UUID id,
    String firstName,
    String middleName,
    String lastName,
    String profilePicture
) {
}
