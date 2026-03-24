package pawtner_core.pawtner_care_api.community.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.community.dto.LikeResponse;
import pawtner_core.pawtner_care_api.community.service.CommunityPostLikeService;

@RestController
@RequestMapping("/api/community/posts/{postId}/likes")
public class CommunityPostLikeController {

    private final CommunityPostLikeService communityPostLikeService;

    public CommunityPostLikeController(CommunityPostLikeService communityPostLikeService) {
        this.communityPostLikeService = communityPostLikeService;
    }

    @PostMapping
    public ResponseEntity<LikeResponse> likePost(
        @PathVariable UUID postId,
        @RequestHeader("X-User-Id") UUID currentUserId
    ) {
        return ResponseEntity.ok(communityPostLikeService.likePost(postId, currentUserId));
    }

    @DeleteMapping
    public ResponseEntity<LikeResponse> unlikePost(
        @PathVariable UUID postId,
        @RequestHeader("X-User-Id") UUID currentUserId
    ) {
        return ResponseEntity.ok(communityPostLikeService.unlikePost(postId, currentUserId));
    }
}

