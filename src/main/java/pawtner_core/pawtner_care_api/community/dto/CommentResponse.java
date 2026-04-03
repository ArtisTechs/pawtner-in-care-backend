package pawtner_core.pawtner_care_api.community.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import pawtner_core.pawtner_care_api.community.enums.CommentStatus;

public record CommentResponse(
    UUID id,
    UUID postId,
    UUID userId,
    CommunityUserSummaryResponse user,
    String content,
    CommentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

