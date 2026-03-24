package pawtner_core.pawtner_care_api.donation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import pawtner_core.pawtner_care_api.donation.dto.DonationCampaignRequest;
import pawtner_core.pawtner_care_api.donation.dto.DonationCampaignResponse;
import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.donation.entity.DonationCampaign;
import pawtner_core.pawtner_care_api.donation.enums.DonationCampaignStatus;
import pawtner_core.pawtner_care_api.donation.enums.DonationCampaignType;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.donation.repository.DonationCampaignRepository;
import pawtner_core.pawtner_care_api.donation.repository.DonationTransactionRepository;

@Service
public class DonationCampaignService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id",
        "title",
        "totalCost",
        "deadline",
        "startDate",
        "updatedDate",
        "isUrgent",
        "status",
        "type"
    );

    private final DonationCampaignRepository donationCampaignRepository;
    private final DonationTransactionRepository donationTransactionRepository;

    public DonationCampaignService(
        DonationCampaignRepository donationCampaignRepository,
        DonationTransactionRepository donationTransactionRepository
    ) {
        this.donationCampaignRepository = donationCampaignRepository;
        this.donationTransactionRepository = donationTransactionRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<DonationCampaignResponse> getDonationCampaigns(
        String search,
        String title,
        DonationCampaignStatus status,
        DonationCampaignType type,
        Boolean isUrgent,
        BigDecimal minTotalCost,
        BigDecimal maxTotalCost,
        LocalDate startDateFrom,
        LocalDate startDateTo,
        LocalDate deadlineFrom,
        LocalDate deadlineTo,
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
        Specification<DonationCampaign> specification = buildDonationCampaignSpecification(
            search,
            title,
            status,
            type,
            isUrgent,
            minTotalCost,
            maxTotalCost,
            startDateFrom,
            startDateTo,
            deadlineFrom,
            deadlineTo
        );

        if (ignorePagination) {
            List<DonationCampaignResponse> content = donationCampaignRepository.findAll(specification, sort).stream()
                .map(this::toResponse)
                .toList();
            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<DonationCampaignResponse> responsePage = donationCampaignRepository.findAll(specification, pageable).map(this::toResponse);
        return PageResponse.fromPage(responsePage, normalizedSortBy, direction.name().toLowerCase(), false);
    }

    @Transactional(readOnly = true)
    public DonationCampaignResponse getDonationCampaign(UUID id) {
        return toResponse(findDonationCampaign(id));
    }

    @Transactional
    public DonationCampaignResponse createDonationCampaign(DonationCampaignRequest request) {
        validateDates(request);

        DonationCampaign donationCampaign = new DonationCampaign();
        applyRequest(donationCampaign, request);

        return toResponse(donationCampaignRepository.save(donationCampaign));
    }

    @Transactional
    public DonationCampaignResponse updateDonationCampaign(UUID id, DonationCampaignRequest request) {
        validateDates(request);

        DonationCampaign donationCampaign = findDonationCampaign(id);
        applyRequest(donationCampaign, request);

        return toResponse(donationCampaignRepository.save(donationCampaign));
    }

    @Transactional
    public void deleteDonationCampaign(UUID id) {
        DonationCampaign donationCampaign = findDonationCampaign(id);
        donationCampaignRepository.delete(donationCampaign);
    }

    private DonationCampaign findDonationCampaign(UUID id) {
        return donationCampaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Donation campaign with id " + id + " was not found"));
    }

    private Specification<DonationCampaign> buildDonationCampaignSpecification(
        String search,
        String title,
        DonationCampaignStatus status,
        DonationCampaignType type,
        Boolean isUrgent,
        BigDecimal minTotalCost,
        BigDecimal maxTotalCost,
        LocalDate startDateFrom,
        LocalDate startDateTo,
        LocalDate deadlineFrom,
        LocalDate deadlineTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            addLikePredicate(predicates, criteriaBuilder, root.get("title"), title);

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            if (isUrgent != null) {
                predicates.add(criteriaBuilder.equal(root.get("isUrgent"), isUrgent));
            }

            addMinAmountPredicate(predicates, criteriaBuilder, root.get("totalCost"), minTotalCost);
            addMaxAmountPredicate(predicates, criteriaBuilder, root.get("totalCost"), maxTotalCost);
            addFromDatePredicate(predicates, criteriaBuilder, root.get("startDate"), startDateFrom);
            addToDatePredicate(predicates, criteriaBuilder, root.get("startDate"), startDateTo);
            addFromDatePredicate(predicates, criteriaBuilder, root.get("deadline"), deadlineFrom);
            addToDatePredicate(predicates, criteriaBuilder, root.get("deadline"), deadlineTo);

            String normalizedSearch = normalizeFilter(search);
            if (normalizedSearch != null) {
                String pattern = "%" + normalizedSearch.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                    )
                );
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void applyRequest(DonationCampaign donationCampaign, DonationCampaignRequest request) {
        donationCampaign.setTitle(request.title().trim());
        donationCampaign.setDescription(normalizeOptionalText(request.description()));
        donationCampaign.setTotalCost(request.totalCost());
        donationCampaign.setDeadline(request.deadline());
        donationCampaign.setStartDate(request.startDate());
        donationCampaign.setPhoto(normalizeOptionalText(request.photo()));
        donationCampaign.setIsUrgent(request.isUrgent());
        donationCampaign.setStatus(request.status());
        donationCampaign.setType(request.type());
    }

    private void validateDates(DonationCampaignRequest request) {
        if (request.startDate() != null && request.deadline() != null && request.deadline().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Deadline cannot be earlier than start date");
        }
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

    private void addMinAmountPredicate(
        List<Predicate> predicates,
        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
        jakarta.persistence.criteria.Path<BigDecimal> path,
        BigDecimal value
    ) {
        if (value != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path, value));
        }
    }

    private void addMaxAmountPredicate(
        List<Predicate> predicates,
        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
        jakarta.persistence.criteria.Path<BigDecimal> path,
        BigDecimal value
    ) {
        if (value != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(path, value));
        }
    }

    private void addFromDatePredicate(
        List<Predicate> predicates,
        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
        jakarta.persistence.criteria.Path<LocalDate> path,
        LocalDate value
    ) {
        if (value != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path, value));
        }
    }

    private void addToDatePredicate(
        List<Predicate> predicates,
        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
        jakarta.persistence.criteria.Path<LocalDate> path,
        LocalDate value
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
            return "id";
        }

        if (!ALLOWED_SORT_FIELDS.contains(requestedSortBy)) {
            throw new IllegalArgumentException("Invalid sortBy value: " + requestedSortBy);
        }

        return requestedSortBy;
    }

    private Sort.Direction normalizeSortDirection(String sortDir) {
        String requestedSortDirection = normalizeFilter(sortDir);
        if (requestedSortDirection == null) {
            return Sort.Direction.ASC;
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

    private DonationCampaignResponse toResponse(DonationCampaign donationCampaign) {
        return new DonationCampaignResponse(
            donationCampaign.getId(),
            donationCampaign.getTitle(),
            donationCampaign.getDescription(),
            donationCampaign.getTotalCost(),
            getTotalDonatedCost(donationCampaign.getId()),
            donationCampaign.getDeadline(),
            donationCampaign.getStartDate(),
            donationCampaign.getUpdatedDate(),
            donationCampaign.getPhoto(),
            donationCampaign.getIsUrgent(),
            donationCampaign.getStatus(),
            donationCampaign.getType()
        );
    }

    private BigDecimal getTotalDonatedCost(UUID donationCampaignId) {
        return donationTransactionRepository.sumDonatedAmountByDonationCampaignId(donationCampaignId);
    }
}

