package pawtner_core.pawtner_care_api.community.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.community.dto.LikeResponse;
import pawtner_core.pawtner_care_api.community.entity.CommunityPost;
import pawtner_core.pawtner_care_api.community.entity.CommunityPostLike;
import pawtner_core.pawtner_care_api.community.mapper.CommunityPostMapper;
import pawtner_core.pawtner_care_api.community.repository.CommunityPostLikeRepository;
import pawtner_core.pawtner_care_api.community.repository.CommunityPostRepository;

@Service
public class CommunityPostLikeService {

    private final CommunityPostService communityPostService;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityUserIntegrationService communityUserIntegrationService;
    private final CommunityPostMapper communityPostMapper;

    public CommunityPostLikeService(
        CommunityPostService communityPostService,
        CommunityPostLikeRepository communityPostLikeRepository,
        CommunityPostRepository communityPostRepository,
        CommunityUserIntegrationService communityUserIntegrationService,
        CommunityPostMapper communityPostMapper
    ) {
        this.communityPostService = communityPostService;
        this.communityPostLikeRepository = communityPostLikeRepository;
        this.communityPostRepository = communityPostRepository;
        this.communityUserIntegrationService = communityUserIntegrationService;
        this.communityPostMapper = communityPostMapper;
    }

    @Transactional
    public LikeResponse likePost(UUID postId, UUID currentUserId) {
        communityUserIntegrationService.validateUserExists(currentUserId);
        CommunityPost post = communityPostService.getAccessiblePostForInteraction(postId, currentUserId);

        if (communityPostLikeRepository.existsByPostIdAndUserId(postId, currentUserId)) {
            return communityPostMapper.toLikeResponse(post, currentUserId, true);
        }

        CommunityPostLike postLike = new CommunityPostLike();
        postLike.setPost(post);
        postLike.setUserId(currentUserId);
        communityPostLikeRepository.save(postLike);

        post.setLikeCount(post.getLikeCount() + 1);
        communityPostRepository.save(post);

        return communityPostMapper.toLikeResponse(post, currentUserId, true);
    }

    @Transactional
    public LikeResponse unlikePost(UUID postId, UUID currentUserId) {
        communityUserIntegrationService.validateUserExists(currentUserId);
        CommunityPost post = communityPostService.getAccessiblePostForInteraction(postId, currentUserId);

        communityPostLikeRepository.findByPostIdAndUserId(postId, currentUserId)
            .ifPresent(postLike -> {
                communityPostLikeRepository.delete(postLike);
                post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                communityPostRepository.save(post);
            });

        return communityPostMapper.toLikeResponse(post, currentUserId, false);
    }
}
