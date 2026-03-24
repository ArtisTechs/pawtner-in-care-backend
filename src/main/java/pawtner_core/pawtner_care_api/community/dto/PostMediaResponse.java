package pawtner_core.pawtner_care_api.community.dto;

import java.util.UUID;

import pawtner_core.pawtner_care_api.community.enums.MediaType;

public record PostMediaResponse(
    UUID id,
    String mediaUrl,
    MediaType mediaType,
    int sortOrder
) {
}
