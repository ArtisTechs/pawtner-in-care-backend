package pawtner_core.pawtner_care_api.community.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pawtner_core.pawtner_care_api.community.enums.PostVisibility;

public record CommunityPostCreateRequest(
    @Size(max = 5000, message = "content must not exceed 5000 characters")
    String content,

    List<@Size(max = 100, message = "hashtag must not exceed 100 characters") String> hashtags,

    @Valid
    List<PostMediaRequest> media,

    @NotNull(message = "visibility is required")
    PostVisibility visibility
) {

    @AssertTrue(message = "Post must have either content or at least one media item")
    public boolean isContentOrMediaProvided() {
        boolean hasContent = content != null && !content.trim().isEmpty();
        boolean hasMedia = media != null && !media.isEmpty();
        return hasContent || hasMedia;
    }
}

