package pawtner_core.pawtner_care_api.community.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import pawtner_core.pawtner_care_api.community.enums.PostStatus;
import pawtner_core.pawtner_care_api.community.enums.PostVisibility;

public record PostResponse(
    UUID postId,
    UUID userId,
    String content,
    PostVisibility visibility,
    PostStatus status,
    long likeCount,
    long commentCount,
    boolean likedByCurrentUser,
    List<HashtagResponse> hashtags,
    List<PostMediaResponse> media,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
