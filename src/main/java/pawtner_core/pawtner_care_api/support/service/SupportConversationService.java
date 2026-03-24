package pawtner_core.pawtner_care_api.support.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.support.dto.SupportConversationDetailsResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportConversationResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportConversationSummaryResponse;
import pawtner_core.pawtner_care_api.support.entity.SupportConversation;
import pawtner_core.pawtner_care_api.support.enums.SupportConversationStatus;
import pawtner_core.pawtner_care_api.support.repository.SupportConversationRepository;
import pawtner_core.pawtner_care_api.support.repository.SupportMessageRepository;

@Service
public class SupportConversationService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "updatedAt", "lastMessageAt", "status", "customerUserId");
    private static final EnumSet<SupportConversationStatus> ACTIVE_STATUSES = EnumSet.of(SupportConversationStatus.OPEN);

    private final SupportConversationRepository supportConversationRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final SupportUserAccessService supportUserAccessService;

    public SupportConversationService(
        SupportConversationRepository supportConversationRepository,
        SupportMessageRepository supportMessageRepository,
        SupportUserAccessService supportUserAccessService
    ) {
        this.supportConversationRepository = supportConversationRepository;
        this.supportMessageRepository = supportMessageRepository;
        this.supportUserAccessService = supportUserAccessService;
    }

    @Transactional(readOnly = true)
    public SupportConversationDetailsResponse getOwnConversation(UUID customerUserId) {
        supportUserAccessService.assertCustomer(customerUserId);
        SupportConversation conversation = findExistingActiveConversation(customerUserId);

        if (conversation == null) {
            return new SupportConversationDetailsResponse(null, 0);
        }

        return new SupportConversationDetailsResponse(
            toConversationResponse(conversation),
            supportMessageRepository.countCustomerUnreadMessages(conversation.getId(), conversation.getCustomerLastReadAt())
        );
    }

    @Transactional(readOnly = true)
    public SupportConversationDetailsResponse getConversationForAdmin(UUID adminUserId, UUID conversationId) {
        supportUserAccessService.assertAdmin(adminUserId);
        SupportConversation conversation = findConversation(conversationId);
        return new SupportConversationDetailsResponse(
            toConversationResponse(conversation),
            supportMessageRepository.countAdminUnreadMessages(conversationId, adminUserId)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<SupportConversationSummaryResponse> getConversationsForAdmin(
        UUID adminUserId,
        UUID customerUserId,
        SupportConversationStatus status,
        boolean unreadOnly,
        int page,
        int size,
        String sortBy,
        String sortDir,
        boolean ignorePagination
    ) {
        supportUserAccessService.assertAdmin(adminUserId);

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction direction = normalizeSortDirection(sortDir);
        Sort sort = Sort.by(direction, normalizedSortBy);
        Specification<SupportConversation> specification = buildSpecification(customerUserId, status);

        if (ignorePagination) {
            List<SupportConversationSummaryResponse> content = supportConversationRepository.findAll(specification, sort).stream()
                .map(conversation -> toSummaryResponse(conversation, adminUserId))
                .filter(response -> !unreadOnly || response.unreadCount() > 0)
                .toList();

            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<SupportConversation> conversationPage = supportConversationRepository.findAll(specification, pageable);
        List<SupportConversationSummaryResponse> filteredContent = conversationPage.getContent().stream()
            .map(conversation -> toSummaryResponse(conversation, adminUserId))
            .filter(response -> !unreadOnly || response.unreadCount() > 0)
            .toList();

        return PageResponse.fromPage(
            new PageImpl<>(
                filteredContent,
                pageable,
                unreadOnly ? filteredContent.size() : conversationPage.getTotalElements()
            ),
            normalizedSortBy,
            direction.name().toLowerCase(),
            false
        );
    }

    @Transactional
    public SupportConversation findOrCreateActiveConversation(UUID customerUserId) {
        supportUserAccessService.assertCustomer(customerUserId);

        return supportConversationRepository
            .findFirstByCustomerUserIdAndStatusInOrderByCreatedAtDesc(customerUserId, ACTIVE_STATUSES)
            .orElseGet(() -> createConversation(customerUserId));
    }

    @Transactional(readOnly = true)
    public SupportConversation findExistingActiveConversation(UUID customerUserId) {
        supportUserAccessService.assertCustomer(customerUserId);
        return findExistingActiveConversationInternal(customerUserId);
    }

    @Transactional(readOnly = true)
    public SupportConversation findConversation(UUID conversationId) {
        return supportConversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResourceNotFoundException("Support conversation with id " + conversationId + " was not found"));
    }

    @Transactional
    public SupportConversation saveConversation(SupportConversation conversation) {
        return supportConversationRepository.save(conversation);
    }

    public SupportConversationResponse toConversationResponse(SupportConversation conversation) {
        return new SupportConversationResponse(
            conversation.getId(),
            conversation.getCustomerUserId(),
            conversation.getStatus(),
            conversation.getLastMessageAt(),
            conversation.getCustomerLastReadAt(),
            conversation.getCreatedAt(),
            conversation.getUpdatedAt()
        );
    }

    public SupportConversationSummaryResponse toSummaryResponse(SupportConversation conversation, UUID adminUserId) {
        return new SupportConversationSummaryResponse(
            conversation.getId(),
            conversation.getCustomerUserId(),
            conversation.getStatus(),
            conversation.getLastMessageAt(),
            conversation.getCustomerLastReadAt(),
            conversation.getCreatedAt(),
            conversation.getUpdatedAt(),
            supportMessageRepository.countAdminUnreadMessages(conversation.getId(), adminUserId)
        );
    }

    public SupportConversationSummaryResponse toCustomerSummaryResponse(SupportConversation conversation) {
        return new SupportConversationSummaryResponse(
            conversation.getId(),
            conversation.getCustomerUserId(),
            conversation.getStatus(),
            conversation.getLastMessageAt(),
            conversation.getCustomerLastReadAt(),
            conversation.getCreatedAt(),
            conversation.getUpdatedAt(),
            supportMessageRepository.countCustomerUnreadMessages(conversation.getId(), conversation.getCustomerLastReadAt())
        );
    }

    private SupportConversation createConversation(UUID customerUserId) {
        SupportConversation conversation = new SupportConversation();
        Instant now = Instant.now();
        conversation.setCustomerUserId(customerUserId);
        conversation.setStatus(SupportConversationStatus.OPEN);
        conversation.setLastMessageAt(now);
        return supportConversationRepository.save(conversation);
    }

    private SupportConversation findExistingActiveConversationInternal(UUID customerUserId) {
        return supportConversationRepository
            .findFirstByCustomerUserIdAndStatusInOrderByCreatedAtDesc(customerUserId, ACTIVE_STATUSES)
            .orElse(null);
    }

    private Specification<SupportConversation> buildSpecification(UUID customerUserId, SupportConversationStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (customerUserId != null) {
                predicates.add(criteriaBuilder.equal(root.get("customerUserId"), customerUserId));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "lastMessageAt";
        }

        String normalizedSortBy = sortBy.trim();
        if (!ALLOWED_SORT_FIELDS.contains(normalizedSortBy)) {
            throw new IllegalArgumentException("Invalid sortBy value: " + normalizedSortBy);
        }

        return normalizedSortBy;
    }

    private Sort.Direction normalizeSortDirection(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return Sort.Direction.DESC;
        }

        try {
            return Sort.Direction.fromString(sortDir.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid sortDir value: " + sortDir);
        }
    }
}

