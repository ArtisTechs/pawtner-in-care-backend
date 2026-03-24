package pawtner_core.pawtner_care_api.community.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pawtner_core.pawtner_care_api.community.dto.CommentResponse;
import pawtner_core.pawtner_care_api.community.dto.CommunityPostCommentCreateRequest;
import pawtner_core.pawtner_care_api.community.dto.CommunityPostCommentUpdateRequest;
import pawtner_core.pawtner_care_api.community.entity.CommunityPost;
import pawtner_core.pawtner_care_api.community.entity.CommunityPostComment;
import pawtner_core.pawtner_care_api.community.enums.CommentStatus;
import pawtner_core.pawtner_care_api.community.mapper.CommunityPostMapper;
import pawtner_core.pawtner_care_api.community.repository.CommunityPostCommentRepository;
import pawtner_core.pawtner_care_api.community.repository.CommunityPostRepository;
import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;

@Service
public class CommunityPostCommentService {

    private final CommunityPostService communityPostService;
    private final CommunityPostCommentRepository communityPostCommentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityUserIntegrationService communityUserIntegrationService;
    private final CommunityPostMapper communityPostMapper;

    public CommunityPostCommentService(
        CommunityPostService communityPostService,
        CommunityPostCommentRepository communityPostCommentRepository,
        CommunityPostRepository communityPostRepository,
        CommunityUserIntegrationService communityUserIntegrationService,
        CommunityPostMapper communityPostMapper
    ) {
        this.communityPostService = communityPostService;
        this.communityPostCommentRepository = communityPostCommentRepository;
        this.communityPostRepository = communityPostRepository;
        this.communityUserIntegrationService = communityUserIntegrationService;
        this.communityPostMapper = communityPostMapper;
    }

    @Transactional
    public CommentResponse createComment(UUID postId, UUID currentUserId, CommunityPostCommentCreateRequest request) {
        communityUserIntegrationService.validateUserExists(currentUserId);
        CommunityPost post = communityPostService.getAccessiblePostForInteraction(postId, currentUserId);

        CommunityPostComment comment = new CommunityPostComment();
        comment.setPost(post);
        comment.setUserId(currentUserId);
        comment.setContent(request.content().trim());
        comment.setStatus(CommentStatus.ACTIVE);

        CommunityPostComment savedComment = communityPostCommentRepository.save(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        communityPostRepository.save(post);

        return communityPostMapper.toCommentResponse(savedComment);
    }

    @Transactional
    public CommentResponse updateComment(
        UUID postId,
        UUID commentId,
        UUID currentUserId,
        CommunityPostCommentUpdateRequest request
    ) {
        CommunityPostComment comment = findActiveComment(commentId);
        validateCommentBelongsToPost(comment, postId);
        validateCommentOwner(comment, currentUserId);

        comment.setContent(request.content().trim());
        return communityPostMapper.toCommentResponse(communityPostCommentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(UUID postId, UUID commentId, UUID currentUserId) {
        CommunityPostComment comment = findActiveComment(commentId);
        validateCommentBelongsToPost(comment, postId);
        validateCommentOwner(comment, currentUserId);

        comment.setStatus(CommentStatus.DELETED);
        comment.setDeletedAt(LocalDateTime.now());
        communityPostCommentRepository.save(comment);

        CommunityPost post = comment.getPost();
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        communityPostRepository.save(post);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentsByPost(
        UUID postId,
        UUID currentUserId,
        int page,
        int size,
        String sortDir,
        boolean ignorePagination
    ) {
        communityPostService.getAccessiblePostForInteraction(postId, currentUserId);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = normalizeSortDirection(sortDir);
        Sort sort = Sort.by(direction, "createdAt");

        if (ignorePagination) {
            Page<CommentResponse> responsePage = communityPostCommentRepository
                .findByPostIdAndStatusAndDeletedAtIsNull(postId, CommentStatus.ACTIVE, Pageable.unpaged(sort))
                .map(communityPostMapper::toCommentResponse);
            return PageResponse.fromList(responsePage.getContent(), "createdAt", direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<CommentResponse> responsePage = communityPostCommentRepository
            .findByPostIdAndStatusAndDeletedAtIsNull(postId, CommentStatus.ACTIVE, pageable)
            .map(communityPostMapper::toCommentResponse);

        return PageResponse.fromPage(responsePage, "createdAt", direction.name().toLowerCase(), false);
    }

    private CommunityPostComment findActiveComment(UUID commentId) {
        CommunityPostComment comment = communityPostCommentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment with id " + commentId + " was not found"));

        if (comment.getStatus() != CommentStatus.ACTIVE || comment.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Comment with id " + commentId + " was not found");
        }

        return comment;
    }

    private void validateCommentBelongsToPost(CommunityPostComment comment, UUID postId) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new ResourceNotFoundException("Comment with id " + comment.getId() + " was not found for post " + postId);
        }
    }

    private void validateCommentOwner(CommunityPostComment comment, UUID currentUserId) {
        if (!comment.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own comments");
        }
    }

    private Sort.Direction normalizeSortDirection(String sortDir) {
        if (sortDir == null || sortDir.trim().isEmpty()) {
            return Sort.Direction.DESC;
        }

        try {
            return Sort.Direction.fromString(sortDir);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid sortDir value: " + sortDir);
        }
    }
}

