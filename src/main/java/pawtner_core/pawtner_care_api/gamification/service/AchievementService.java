package pawtner_core.pawtner_care_api.gamification.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.gamification.dto.AchievementResponse;
import pawtner_core.pawtner_care_api.gamification.dto.AchievementStatusUpdateRequest;
import pawtner_core.pawtner_care_api.gamification.dto.AchievementUpsertRequest;
import pawtner_core.pawtner_care_api.gamification.dto.ManualAchievementAssignmentRequest;
import pawtner_core.pawtner_care_api.gamification.dto.UserAchievementResponse;
import pawtner_core.pawtner_care_api.gamification.entity.Achievement;
import pawtner_core.pawtner_care_api.gamification.entity.UserAchievement;
import pawtner_core.pawtner_care_api.gamification.repository.AchievementRepository;
import pawtner_core.pawtner_care_api.gamification.repository.UserAchievementRepository;

@Service
public class AchievementService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id",
        "code",
        "title",
        "category",
        "points",
        "rarity",
        "isActive",
        "isRepeatable",
        "visibility",
        "assignmentType",
        "triggerType",
        "ruleType",
        "startAt",
        "endAt",
        "createdAt",
        "updatedAt"
    );

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementEvaluationService achievementEvaluationService;
    private final AchievementMapperService achievementMapperService;

    public AchievementService(
        AchievementRepository achievementRepository,
        UserAchievementRepository userAchievementRepository,
        AchievementEvaluationService achievementEvaluationService,
        AchievementMapperService achievementMapperService
    ) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.achievementEvaluationService = achievementEvaluationService;
        this.achievementMapperService = achievementMapperService;
    }

    @Transactional
    public AchievementResponse createAchievement(AchievementUpsertRequest request) {
        String normalizedCode = normalizeCode(request.code());
        validateCreateCode(normalizedCode);

        Achievement achievement = new Achievement();
        applyAchievementRequest(achievement, request, normalizedCode);
        return achievementMapperService.toAchievementResponse(achievementRepository.save(achievement));
    }

    @Transactional
    public AchievementResponse updateAchievement(UUID id, AchievementUpsertRequest request) {
        Achievement achievement = findAchievement(id);
        String normalizedCode = normalizeCode(request.code());
        validateUpdateCode(normalizedCode, id);

        applyAchievementRequest(achievement, request, normalizedCode);
        return achievementMapperService.toAchievementResponse(achievementRepository.save(achievement));
    }

    @Transactional(readOnly = true)
    public PageResponse<AchievementResponse> getAchievements(
        String search,
        String code,
        String title,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementCategory category,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementRarity rarity,
        Boolean isActive,
        Boolean isRepeatable,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementVisibility visibility,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementAssignmentType assignmentType,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementTriggerType triggerType,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementRuleType ruleType,
        LocalDateTime startAtFrom,
        LocalDateTime startAtTo,
        LocalDateTime endAtFrom,
        LocalDateTime endAtTo,
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
        Specification<Achievement> specification = buildAchievementSpecification(
            search,
            code,
            title,
            category,
            rarity,
            isActive,
            isRepeatable,
            visibility,
            assignmentType,
            triggerType,
            ruleType,
            startAtFrom,
            startAtTo,
            endAtFrom,
            endAtTo
        );

        if (ignorePagination) {
            List<AchievementResponse> content = achievementRepository.findAll(specification, sort).stream()
                .map(achievementMapperService::toAchievementResponse)
                .toList();
            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<AchievementResponse> responsePage = achievementRepository.findAll(specification, pageable)
            .map(achievementMapperService::toAchievementResponse);
        return PageResponse.fromPage(responsePage, normalizedSortBy, direction.name().toLowerCase(), false);
    }

    @Transactional(readOnly = true)
    public AchievementResponse getAchievement(UUID id) {
        return achievementMapperService.toAchievementResponse(findAchievement(id));
    }

    @Transactional
    public AchievementResponse updateAchievementStatus(UUID id, AchievementStatusUpdateRequest request) {
        Achievement achievement = findAchievement(id);
        achievement.setIsActive(request.isActive());
        return achievementMapperService.toAchievementResponse(achievementRepository.save(achievement));
    }

    @Transactional
    public void deleteAchievement(UUID id) {
        Achievement achievement = findAchievement(id);
        if (userAchievementRepository.existsByAchievement_Id(id)) {
            throw new IllegalArgumentException("Achievement cannot be deleted because it has linked user achievements");
        }

        achievementRepository.delete(achievement);
    }

    @Transactional
    public UserAchievementResponse manuallyAssignAchievement(ManualAchievementAssignmentRequest request) {
        Achievement achievement = resolveAchievement(request.achievementId(), request.achievementCode());
        UserAchievement userAchievement = achievementEvaluationService.manuallyAssign(
            request.userId(),
            achievement.getId(),
            "MANUAL_ASSIGNMENT",
            request.metadata()
        );
        return achievementMapperService.toUserAchievementResponse(userAchievement);
    }

    @Transactional(readOnly = true)
    public List<UserAchievementResponse> getUserAchievements(UUID userId) {
        return userAchievementRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .sorted(Comparator.comparing(UserAchievement::getCreatedAt).reversed())
            .map(achievementMapperService::toUserAchievementResponse)
            .toList();
    }

    private Achievement findAchievement(UUID id) {
        return achievementRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Achievement with id " + id + " was not found"));
    }

    private void applyAchievementRequest(Achievement achievement, AchievementUpsertRequest request, String normalizedCode) {
        validateDateRange(request.startAt(), request.endAt());

        achievement.setCode(normalizedCode);
        achievement.setTitle(request.title().trim());
        achievement.setDescription(request.description().trim());
        achievement.setIconUrl(normalizeOptionalText(request.iconUrl()));
        achievement.setCategory(request.category());
        achievement.setPoints(request.points());
        achievement.setRarity(request.rarity());
        achievement.setIsActive(request.isActive());
        achievement.setIsRepeatable(request.isRepeatable());
        achievement.setVisibility(request.visibility());
        achievement.setAssignmentType(request.assignmentType());
        achievement.setTriggerType(request.triggerType());
        achievement.setRuleType(request.ruleType());
        achievement.setRuleConfig(normalizeOptionalText(request.ruleConfig()));
        achievement.setStartAt(request.startAt());
        achievement.setEndAt(request.endAt());
    }

    private void validateCreateCode(String code) {
        if (achievementRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Achievement code is already in use");
        }
    }

    private void validateUpdateCode(String code, UUID id) {
        if (achievementRepository.existsByCodeAndIdNot(code, id)) {
            throw new IllegalArgumentException("Achievement code is already in use");
        }
    }

    private void validateDateRange(java.time.LocalDateTime startAt, java.time.LocalDateTime endAt) {
        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("End date cannot be earlier than start date");
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private Specification<Achievement> buildAchievementSpecification(
        String search,
        String code,
        String title,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementCategory category,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementRarity rarity,
        Boolean isActive,
        Boolean isRepeatable,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementVisibility visibility,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementAssignmentType assignmentType,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementTriggerType triggerType,
        pawtner_core.pawtner_care_api.gamification.enums.AchievementRuleType ruleType,
        LocalDateTime startAtFrom,
        LocalDateTime startAtTo,
        LocalDateTime endAtFrom,
        LocalDateTime endAtTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            addLikePredicate(predicates, criteriaBuilder, root.get("code"), code);
            addLikePredicate(predicates, criteriaBuilder, root.get("title"), title);

            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            if (rarity != null) {
                predicates.add(criteriaBuilder.equal(root.get("rarity"), rarity));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            if (isRepeatable != null) {
                predicates.add(criteriaBuilder.equal(root.get("isRepeatable"), isRepeatable));
            }

            if (visibility != null) {
                predicates.add(criteriaBuilder.equal(root.get("visibility"), visibility));
            }

            if (assignmentType != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignmentType"), assignmentType));
            }

            if (triggerType != null) {
                predicates.add(criteriaBuilder.equal(root.get("triggerType"), triggerType));
            }

            if (ruleType != null) {
                predicates.add(criteriaBuilder.equal(root.get("ruleType"), ruleType));
            }

            addFromDateTimePredicate(predicates, criteriaBuilder, root.get("startAt"), startAtFrom);
            addToDateTimePredicate(predicates, criteriaBuilder, root.get("startAt"), startAtTo);
            addFromDateTimePredicate(predicates, criteriaBuilder, root.get("endAt"), endAtFrom);
            addToDateTimePredicate(predicates, criteriaBuilder, root.get("endAt"), endAtTo);

            String normalizedSearch = normalizeFilter(search);
            if (normalizedSearch != null) {
                String pattern = "%" + normalizedSearch.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                    )
                );
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void addLikePredicate(
        List<Predicate> predicates,
        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
        jakarta.persistence.criteria.Path<String> path,
        String value
    ) {
        String normalizedValue = normalizeFilter(value);
        if (normalizedValue != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(path), "%" + normalizedValue.toLowerCase() + "%"));
        }
    }

    private void addFromDateTimePredicate(
        List<Predicate> predicates,
        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
        jakarta.persistence.criteria.Path<LocalDateTime> path,
        LocalDateTime value
    ) {
        if (value != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path, value));
        }
    }

    private void addToDateTimePredicate(
        List<Predicate> predicates,
        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
        jakarta.persistence.criteria.Path<LocalDateTime> path,
        LocalDateTime value
    ) {
        if (value != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(path, value));
        }
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private String normalizeSortBy(String sortBy) {
        String requestedSortBy = normalizeFilter(sortBy);
        if (requestedSortBy == null) {
            return "createdAt";
        }

        if (!ALLOWED_SORT_FIELDS.contains(requestedSortBy)) {
            throw new IllegalArgumentException("Invalid sortBy value: " + requestedSortBy);
        }

        return requestedSortBy;
    }

    private Sort.Direction normalizeSortDirection(String sortDir) {
        String requestedSortDirection = normalizeFilter(sortDir);
        if (requestedSortDirection == null) {
            return Sort.Direction.DESC;
        }

        try {
            return Sort.Direction.fromString(requestedSortDirection);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid sortDir value: " + requestedSortDirection);
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private Achievement resolveAchievement(UUID achievementId, String achievementCode) {
        if (achievementId != null) {
            return findAchievement(achievementId);
        }

        if (achievementCode != null && !achievementCode.isBlank()) {
            return achievementRepository.findByCode(achievementCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Achievement with code " + achievementCode + " was not found"));
        }

        throw new IllegalArgumentException("Either achievementId or achievementCode is required");
    }
}

