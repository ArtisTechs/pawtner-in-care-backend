package pawtner_core.pawtner_care_api.community.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pawtner_core.pawtner_care_api.community.dto.CommunityPostCreateRequest;
import pawtner_core.pawtner_care_api.community.dto.CommunityPostUpdateRequest;
import pawtner_core.pawtner_care_api.community.dto.FeedItemResponse;
import pawtner_core.pawtner_care_api.community.dto.PostMediaRequest;
import pawtner_core.pawtner_care_api.community.dto.PostResponse;
import pawtner_core.pawtner_care_api.community.entity.CommunityPost;
import pawtner_core.pawtner_care_api.community.entity.CommunityPostHashtag;
import pawtner_core.pawtner_care_api.community.entity.CommunityPostLike;
import pawtner_core.pawtner_care_api.community.entity.CommunityPostMedia;
import pawtner_core.pawtner_care_api.community.entity.Hashtag;
import pawtner_core.pawtner_care_api.community.enums.PostStatus;
import pawtner_core.pawtner_care_api.community.enums.PostVisibility;
import pawtner_core.pawtner_care_api.community.mapper.CommunityPostMapper;
import pawtner_core.pawtner_care_api.community.repository.CommunityPostHashtagRepository;
import pawtner_core.pawtner_care_api.community.repository.CommunityPostLikeRepository;
import pawtner_core.pawtner_care_api.community.repository.CommunityPostMediaRepository;
import pawtner_core.pawtner_care_api.community.repository.CommunityPostRepository;
import pawtner_core.pawtner_care_api.dto.PageResponse;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;

@Service
public class CommunityPostService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "updatedAt", "likeCount", "commentCount");

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostMediaRepository communityPostMediaRepository;
    private final CommunityPostHashtagRepository communityPostHashtagRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityHashtagService communityHashtagService;
    private final CommunityUserIntegrationService communityUserIntegrationService;
    private final CommunityPostMapper communityPostMapper;

    public CommunityPostService(
        CommunityPostRepository communityPostRepository,
        CommunityPostMediaRepository communityPostMediaRepository,
        CommunityPostHashtagRepository communityPostHashtagRepository,
        CommunityPostLikeRepository communityPostLikeRepository,
        CommunityHashtagService communityHashtagService,
        CommunityUserIntegrationService communityUserIntegrationService,
        CommunityPostMapper communityPostMapper
    ) {
        this.communityPostRepository = communityPostRepository;
        this.communityPostMediaRepository = communityPostMediaRepository;
        this.communityPostHashtagRepository = communityPostHashtagRepository;
        this.communityPostLikeRepository = communityPostLikeRepository;
        this.communityHashtagService = communityHashtagService;
        this.communityUserIntegrationService = communityUserIntegrationService;
        this.communityPostMapper = communityPostMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<FeedItemResponse> getFeed(
        UUID currentUserId,
        UUID userId,
        String hashtag,
        String keyword,
        int page,
        int size,
        String sortBy,
        String sortDir,
        boolean ignorePagination
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction direction = normalizeSortDirection(sortDir);
        Sort sort = Sort.by(direction, normalizedSortBy);
        Specification<CommunityPost> specification = buildFeedSpecification(currentUserId, userId, hashtag, keyword);

        if (ignorePagination) {
            List<FeedItemResponse> content = mapPostsToFeedItems(
                communityPostRepository.findAll(specification, sort),
                currentUserId
            );
            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<CommunityPost> posts = communityPostRepository.findAll(specification, pageable);
        List<FeedItemResponse> mappedContent = mapPostsToFeedItems(posts.getContent(), currentUserId);
        Page<FeedItemResponse> responsePage = new PageImpl<>(mappedContent, pageable, posts.getTotalElements());
        return PageResponse.fromPage(responsePage, normalizedSortBy, direction.name().toLowerCase(), false);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(UUID postId, UUID currentUserId) {
        CommunityPost post = findPost(postId);
        validatePostAccessible(post, currentUserId);
        return mapPost(post, currentUserId);
    }

    @Transactional
    public PostResponse createPost(UUID currentUserId, CommunityPostCreateRequest request) {
        communityUserIntegrationService.validateUserExists(currentUserId);

        CommunityPost post = new CommunityPost();
        post.setUserId(currentUserId);
        post.setContent(normalizeOptionalContent(request.content()));
        post.setVisibility(request.visibility());
        post.setStatus(PostStatus.ACTIVE);
        post.setLikeCount(0);
        post.setCommentCount(0);

        CommunityPost savedPost = communityPostRepository.save(post);
        replaceMedia(savedPost, request.media());
        replaceHashtags(savedPost, request.content(), request.hashtags());

        return mapPost(savedPost, currentUserId);
    }

    @Transactional
    public PostResponse updatePost(UUID postId, UUID currentUserId, CommunityPostUpdateRequest request) {
        CommunityPost post = findPost(postId);
        validatePostOwner(post, currentUserId);
        validatePostEditable(post);

        post.setContent(normalizeOptionalContent(request.content()));
        post.setVisibility(request.visibility());
        post.setDeletedAt(null);
        post.setStatus(PostStatus.ACTIVE);

        replaceMedia(post, request.media());
        replaceHashtags(post, request.content(), request.hashtags());

        return mapPost(communityPostRepository.save(post), currentUserId);
    }

    @Transactional
    public void deletePost(UUID postId, UUID currentUserId) {
        CommunityPost post = findPost(postId);
        validatePostOwner(post, currentUserId);

        if (post.getStatus() == PostStatus.DELETED) {
            return;
        }

        post.setStatus(PostStatus.DELETED);
        post.setDeletedAt(LocalDateTime.now());
        communityPostRepository.save(post);
    }

    public URI buildPostLocation(UUID postId) {
        return URI.create("/api/community/posts/" + postId);
    }

    public CommunityPost getAccessiblePostForInteraction(UUID postId, UUID currentUserId) {
        CommunityPost post = findPost(postId);
        validatePostAccessible(post, currentUserId);
        return post;
    }

    private Specification<CommunityPost> buildFeedSpecification(
        UUID currentUserId,
        UUID userId,
        String hashtag,
        String keyword
    ) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), PostStatus.ACTIVE));
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));

                if (!userId.equals(currentUserId)) {
                    predicates.add(criteriaBuilder.equal(root.get("visibility"), PostVisibility.PUBLIC));
                }
            } else {
                predicates.add(criteriaBuilder.equal(root.get("visibility"), PostVisibility.PUBLIC));
            }

            String normalizedKeyword = normalizeFilter(keyword);
            if (normalizedKeyword != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), "%" + normalizedKeyword.toLowerCase() + "%"));
            }

            String normalizedHashtag = communityHashtagService.normalizeHashtag(hashtag);
            if (!normalizedHashtag.isBlank()) {
                Join<Object, Object> postHashtagJoin = root.join("hashtags", JoinType.INNER);
                Join<Object, Object> hashtagJoin = postHashtagJoin.join("hashtag", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(hashtagJoin.get("normalizedName"), normalizedHashtag));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private List<FeedItemResponse> mapPostsToFeedItems(List<CommunityPost> posts, UUID currentUserId) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<UUID> postIds = extractPostIds(posts);
        Map<UUID, List<CommunityPostMedia>> mediaByPostId = getMediaByPostId(postIds);
        Map<UUID, List<Hashtag>> hashtagsByPostId = getHashtagsByPostId(postIds);
        Set<UUID> likedPostIds = getLikedPostIds(postIds, currentUserId);

        return posts.stream()
            .map(post -> communityPostMapper.toFeedItemResponse(
                post,
                mediaByPostId.getOrDefault(post.getId(), List.of()),
                hashtagsByPostId.getOrDefault(post.getId(), List.of()),
                likedPostIds.contains(post.getId())
            ))
            .toList();
    }

    private PostResponse mapPost(CommunityPost post, UUID currentUserId) {
        List<UUID> postIds = List.of(post.getId());
        Map<UUID, List<CommunityPostMedia>> mediaByPostId = getMediaByPostId(postIds);
        Map<UUID, List<Hashtag>> hashtagsByPostId = getHashtagsByPostId(postIds);
        Set<UUID> likedPostIds = getLikedPostIds(postIds, currentUserId);

        return communityPostMapper.toPostResponse(
            post,
            mediaByPostId.getOrDefault(post.getId(), List.of()),
            hashtagsByPostId.getOrDefault(post.getId(), List.of()),
            likedPostIds.contains(post.getId())
        );
    }

    private Map<UUID, List<CommunityPostMedia>> getMediaByPostId(Collection<UUID> postIds) {
        Map<UUID, List<CommunityPostMedia>> mediaByPostId = new HashMap<>();
        communityPostMediaRepository.findByPostIdInOrderByPostIdAscSortOrderAscCreatedAtAsc(postIds)
            .forEach(media -> mediaByPostId.computeIfAbsent(media.getPost().getId(), ignored -> new ArrayList<>()).add(media));
        return mediaByPostId;
    }

    private Map<UUID, List<Hashtag>> getHashtagsByPostId(Collection<UUID> postIds) {
        Map<UUID, List<Hashtag>> hashtagsByPostId = new LinkedHashMap<>();
        communityPostHashtagRepository.findByPostIdIn(postIds)
            .forEach(postHashtag -> hashtagsByPostId
                .computeIfAbsent(postHashtag.getPost().getId(), ignored -> new ArrayList<>())
                .add(postHashtag.getHashtag()));
        return hashtagsByPostId;
    }

    private Set<UUID> getLikedPostIds(Collection<UUID> postIds, UUID currentUserId) {
        if (currentUserId == null || postIds.isEmpty()) {
            return Set.of();
        }

        return communityPostLikeRepository.findByPostIdInAndUserId(postIds, currentUserId).stream()
            .map(CommunityPostLike::getPost)
            .map(CommunityPost::getId)
            .collect(Collectors.toSet());
    }

    private List<UUID> extractPostIds(List<CommunityPost> posts) {
        return posts.stream().map(CommunityPost::getId).toList();
    }

    private void replaceMedia(CommunityPost post, List<PostMediaRequest> mediaRequests) {
        communityPostMediaRepository.deleteByPostId(post.getId());

        if (mediaRequests == null || mediaRequests.isEmpty()) {
            return;
        }

        List<CommunityPostMedia> mediaItems = mediaRequests.stream()
            .map(mediaRequest -> toMediaEntity(post, mediaRequest))
            .toList();
        communityPostMediaRepository.saveAll(mediaItems);
    }

    private CommunityPostMedia toMediaEntity(CommunityPost post, PostMediaRequest mediaRequest) {
        CommunityPostMedia media = new CommunityPostMedia();
        media.setPost(post);
        media.setMediaUrl(mediaRequest.mediaUrl().trim());
        media.setMediaType(mediaRequest.mediaType());
        media.setSortOrder(mediaRequest.sortOrder());
        return media;
    }

    private void replaceHashtags(CommunityPost post, String content, List<String> requestHashtags) {
        communityPostHashtagRepository.deleteByPostId(post.getId());

        List<Hashtag> hashtags = communityHashtagService.resolveHashtags(content, requestHashtags);
        if (hashtags.isEmpty()) {
            return;
        }

        List<CommunityPostHashtag> postHashtags = hashtags.stream()
            .map(hashtag -> {
                CommunityPostHashtag postHashtag = new CommunityPostHashtag();
                postHashtag.setPost(post);
                postHashtag.setHashtag(hashtag);
                return postHashtag;
            })
            .toList();

        communityPostHashtagRepository.saveAll(postHashtags);
    }

    private CommunityPost findPost(UUID postId) {
        return communityPostRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Community post with id " + postId + " was not found"));
    }

    private void validatePostAccessible(CommunityPost post, UUID currentUserId) {
        if (post.getStatus() != PostStatus.ACTIVE || post.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community post with id " + post.getId() + " was not found");
        }

        if (post.getVisibility() == PostVisibility.PRIVATE && !post.getUserId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Community post with id " + post.getId() + " was not found");
        }
    }

    private void validatePostOwner(CommunityPost post, UUID currentUserId) {
        if (!post.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own posts");
        }
    }

    private void validatePostEditable(CommunityPost post) {
        if (post.getStatus() != PostStatus.ACTIVE || post.getDeletedAt() != null) {
            throw new IllegalArgumentException("Only active posts can be updated");
        }
    }

    private String normalizeOptionalContent(String content) {
        if (content == null) {
            return null;
        }

        String trimmed = content.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeSortBy(String sortBy) {
        String normalizedSortBy = normalizeFilter(sortBy);
        if (normalizedSortBy == null) {
            return "createdAt";
        }

        if (!ALLOWED_SORT_FIELDS.contains(normalizedSortBy)) {
            throw new IllegalArgumentException("Invalid sortBy value: " + normalizedSortBy);
        }

        return normalizedSortBy;
    }

    private Sort.Direction normalizeSortDirection(String sortDir) {
        String normalizedSortDirection = normalizeFilter(sortDir);
        if (normalizedSortDirection == null) {
            return Sort.Direction.DESC;
        }

        try {
            return Sort.Direction.fromString(normalizedSortDirection);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid sortDir value: " + normalizedSortDirection);
        }
    }
}
