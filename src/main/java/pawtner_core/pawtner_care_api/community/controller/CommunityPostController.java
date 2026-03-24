package pawtner_core.pawtner_care_api.community.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.community.dto.CommunityPostCreateRequest;
import pawtner_core.pawtner_care_api.community.dto.CommunityPostUpdateRequest;
import pawtner_core.pawtner_care_api.community.dto.FeedItemResponse;
import pawtner_core.pawtner_care_api.community.dto.PostResponse;
import pawtner_core.pawtner_care_api.community.service.CommunityPostService;
import pawtner_core.pawtner_care_api.common.dto.PageResponse;

@RestController
@RequestMapping("/api/community/posts")
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    public CommunityPostController(CommunityPostService communityPostService) {
        this.communityPostService = communityPostService;
    }

    @GetMapping
    public PageResponse<FeedItemResponse> getFeed(
        @RequestHeader(value = "X-User-Id", required = false) UUID currentUserId,
        @RequestParam(required = false) UUID userId,
        @RequestParam(required = false) String hashtag,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(defaultValue = "false") boolean ignorePagination
    ) {
        return communityPostService.getFeed(currentUserId, userId, hashtag, keyword, page, size, sortBy, sortDir, ignorePagination);
    }

    @GetMapping("/{postId}")
    public PostResponse getPost(
        @PathVariable UUID postId,
        @RequestHeader(value = "X-User-Id", required = false) UUID currentUserId
    ) {
        return communityPostService.getPost(postId, currentUserId);
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
        @RequestHeader("X-User-Id") UUID currentUserId,
        @Valid @RequestBody CommunityPostCreateRequest request
    ) {
        PostResponse response = communityPostService.createPost(currentUserId, request);
        URI location = communityPostService.buildPostLocation(response.postId());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{postId}")
    public PostResponse updatePost(
        @PathVariable UUID postId,
        @RequestHeader("X-User-Id") UUID currentUserId,
        @Valid @RequestBody CommunityPostUpdateRequest request
    ) {
        return communityPostService.updatePost(postId, currentUserId, request);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
        @PathVariable UUID postId,
        @RequestHeader("X-User-Id") UUID currentUserId
    ) {
        communityPostService.deletePost(postId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}

