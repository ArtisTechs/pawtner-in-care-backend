package pawtner_core.pawtner_care_api.community.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pawtner_core.pawtner_care_api.community.enums.MediaType;

public record PostMediaRequest(
    @NotBlank(message = "mediaUrl is required")
    @Size(max = 1000, message = "mediaUrl must not exceed 1000 characters")
    String mediaUrl,

    @NotNull(message = "mediaType is required")
    MediaType mediaType,

    @Min(value = 0, message = "sortOrder must be greater than or equal to 0")
    int sortOrder
) {
}

