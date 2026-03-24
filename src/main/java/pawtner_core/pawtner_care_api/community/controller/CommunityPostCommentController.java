package pawtner_core.pawtner_care_api.community.controller;

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

import pawtner_core.pawtner_care_api.community.dto.CommentResponse;
import pawtner_core.pawtner_care_api.community.dto.CommunityPostCommentCreateRequest;
import pawtner_core.pawtner_care_api.community.dto.CommunityPostCommentUpdateRequest;
import pawtner_core.pawtner_care_api.community.service.CommunityPostCommentService;
import pawtner_core.pawtner_care_api.common.dto.PageResponse;

@RestController
@RequestMapping("/api/community/posts/{postId}/comments")
public class CommunityPostCommentController {

    private final CommunityPostCommentService communityPostCommentService;

    public CommunityPostCommentController(CommunityPostCommentService communityPostCommentService) {
        this.communityPostCommentService = communityPostCommentService;
    }

    @GetMapping
    public PageResponse<CommentResponse> getComments(
        @PathVariable UUID postId,
        @RequestHeader(value = "X-User-Id", required = false) UUID currentUserId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(defaultValue = "false") boolean ignorePagination
    ) {
        return communityPostCommentService.getCommentsByPost(postId, currentUserId, page, size, sortDir, ignorePagination);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
        @PathVariable UUID postId,
        @RequestHeader("X-User-Id") UUID currentUserId,
        @Valid @RequestBody CommunityPostCommentCreateRequest request
    ) {
        return ResponseEntity.ok(communityPostCommentService.createComment(postId, currentUserId, request));
    }

    @PutMapping("/{commentId}")
    public CommentResponse updateComment(
        @PathVariable UUID postId,
        @PathVariable UUID commentId,
        @RequestHeader("X-User-Id") UUID currentUserId,
        @Valid @RequestBody CommunityPostCommentUpdateRequest request
    ) {
        return communityPostCommentService.updateComment(postId, commentId, currentUserId, request);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
        @PathVariable UUID postId,
        @PathVariable UUID commentId,
        @RequestHeader("X-User-Id") UUID currentUserId
    ) {
        communityPostCommentService.deleteComment(postId, commentId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}

