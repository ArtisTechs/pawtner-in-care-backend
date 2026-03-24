package pawtner_core.pawtner_care_api.community.dto;

import java.util.UUID;

public record HashtagResponse(
    UUID id,
    String name,
    String normalizedName
) {
}
