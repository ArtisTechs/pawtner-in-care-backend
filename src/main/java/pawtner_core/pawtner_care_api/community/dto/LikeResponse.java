package pawtner_core.pawtner_care_api.community.dto;

import java.util.UUID;

public record LikeResponse(
    UUID postId,
    UUID userId,
    boolean liked,
    long likeCount
) {
}

