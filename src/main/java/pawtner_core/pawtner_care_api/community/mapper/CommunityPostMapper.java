package pawtner_core.pawtner_care_api.community.mapper;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import pawtner_core.pawtner_care_api.community.dto.CommentResponse;
import pawtner_core.pawtner_care_api.community.dto.FeedItemResponse;
import pawtner_core.pawtner_care_api.community.dto.HashtagResponse;
import pawtner_core.pawtner_care_api.community.dto.LikeResponse;
import pawtner_core.pawtner_care_api.community.dto.PostMediaResponse;
import pawtner_core.pawtner_care_api.community.dto.PostResponse;
import pawtner_core.pawtner_care_api.community.entity.CommunityPost;
import pawtner_core.pawtner_care_api.community.entity.CommunityPostComment;
import pawtner_core.pawtner_care_api.community.entity.CommunityPostMedia;
import pawtner_core.pawtner_care_api.community.entity.Hashtag;

@Service
public class CommunityPostMapper {

    public PostResponse toPostResponse(
        CommunityPost post,
        List<CommunityPostMedia> mediaItems,
        List<Hashtag> hashtags,
        boolean likedByCurrentUser
    ) {
        return new PostResponse(
            post.getId(),
            post.getUserId(),
            post.getContent(),
            post.getVisibility(),
            post.getStatus(),
            post.getLikeCount(),
            post.getCommentCount(),
            likedByCurrentUser,
            hashtags.stream().map(this::toHashtagResponse).toList(),
            mediaItems.stream().map(this::toMediaResponse).toList(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }

    public FeedItemResponse toFeedItemResponse(
        CommunityPost post,
        List<CommunityPostMedia> mediaItems,
        List<Hashtag> hashtags,
        boolean likedByCurrentUser
    ) {
        return new FeedItemResponse(
            post.getId(),
            post.getUserId(),
            post.getContent(),
            post.getVisibility(),
            post.getStatus(),
            post.getLikeCount(),
            post.getCommentCount(),
            likedByCurrentUser,
            hashtags.stream().map(this::toHashtagResponse).toList(),
            mediaItems.stream().map(this::toMediaResponse).toList(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }

    public CommentResponse toCommentResponse(CommunityPostComment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getPost().getId(),
            comment.getUserId(),
            comment.getContent(),
            comment.getStatus(),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }

    public LikeResponse toLikeResponse(CommunityPost post, UUID userId, boolean liked) {
        return new LikeResponse(post.getId(), userId, liked, post.getLikeCount());
    }

    public HashtagResponse toHashtagResponse(Hashtag hashtag) {
        return new HashtagResponse(hashtag.getId(), hashtag.getName(), hashtag.getNormalizedName());
    }

    public PostMediaResponse toMediaResponse(CommunityPostMedia media) {
        return new PostMediaResponse(media.getId(), media.getMediaUrl(), media.getMediaType(), media.getSortOrder());
    }
}

