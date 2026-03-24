package pawtner_core.pawtner_care_api.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunityPostCommentCreateRequest(
    @NotBlank(message = "content is required")
    @Size(max = 3000, message = "content must not exceed 3000 characters")
    String content
) {
}
