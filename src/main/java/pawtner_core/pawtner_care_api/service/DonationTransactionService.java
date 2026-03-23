package pawtner_core.pawtner_care_api.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.dto.PageResponse;
import pawtner_core.pawtner_care_api.dto.DonationTransactionRequest;
import pawtner_core.pawtner_care_api.dto.DonationTransactionResponse;
import pawtner_core.pawtner_care_api.entity.DonationCampaign;
import pawtner_core.pawtner_care_api.entity.DonationTransaction;
import pawtner_core.pawtner_care_api.entity.PaymentMode;
import pawtner_core.pawtner_care_api.entity.User;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.repository.DonationCampaignRepository;
import pawtner_core.pawtner_care_api.repository.DonationTransactionRepository;
import pawtner_core.pawtner_care_api.repository.PaymentModeRepository;
import pawtner_core.pawtner_care_api.repository.UserRepository;

@Service
public class DonationTransactionService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id",
        "donatedAmount",
        "user.firstName",
        "user.lastName",
        "user.email",
        "paymentMode.name",
        "donationCampaign.title"
    );

    private final DonationTransactionRepository donationTransactionRepository;
    private final PaymentModeRepository paymentModeRepository;
    private final DonationCampaignRepository donationCampaignRepository;
    private final UserRepository userRepository;

    public DonationTransactionService(
        DonationTransactionRepository donationTransactionRepository,
        PaymentModeRepository paymentModeRepository,
        DonationCampaignRepository donationCampaignRepository,
        UserRepository userRepository
    ) {
        this.donationTransactionRepository = donationTransactionRepository;
        this.paymentModeRepository = paymentModeRepository;
        this.donationCampaignRepository = donationCampaignRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<DonationTransactionResponse> getDonationTransactions(
        String search,
        UUID userId,
        UUID paymentModeId,
        UUID donationCampaignId,
        String userName,
        String userEmail,
        String paymentModeName,
        String donationCampaignTitle,
        BigDecimal minDonatedAmount,
        BigDecimal maxDonatedAmount,
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
        Specification<DonationTransaction> specification = buildDonationTransactionSpecification(
            search,
            userId,
            paymentModeId,
            donationCampaignId,
            userName,
            userEmail,
            paymentModeName,
            donationCampaignTitle,
            minDonatedAmount,
            maxDonatedAmount
        );

        if (ignorePagination) {
            List<DonationTransactionResponse> content = donationTransactionRepository.findAll(specification, sort).stream()
                .map(this::toResponse)
                .toList();
            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<DonationTransactionResponse> responsePage =
            donationTransactionRepository.findAll(specification, pageable).map(this::toResponse);
        return PageResponse.fromPage(responsePage, normalizedSortBy, direction.name().toLowerCase(), false);
    }

    @Transactional(readOnly = true)
    public DonationTransactionResponse getDonationTransaction(UUID id) {
        return toResponse(findDonationTransaction(id));
    }

    @Transactional
    public DonationTransactionResponse createDonationTransaction(DonationTransactionRequest request) {
        DonationTransaction donationTransaction = new DonationTransaction();
        applyRequest(donationTransaction, request);

        return toResponse(donationTransactionRepository.save(donationTransaction));
    }

    @Transactional
    public DonationTransactionResponse updateDonationTransaction(UUID id, DonationTransactionRequest request) {
        DonationTransaction donationTransaction = findDonationTransaction(id);
        applyRequest(donationTransaction, request);

        return toResponse(donationTransactionRepository.save(donationTransaction));
    }

    @Transactional
    public void deleteDonationTransaction(UUID id) {
        DonationTransaction donationTransaction = findDonationTransaction(id);
        donationTransactionRepository.delete(donationTransaction);
    }

    private DonationTransaction findDonationTransaction(UUID id) {
        return donationTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Donation transaction with id " + id + " was not found"));
    }

    private PaymentMode findPaymentMode(UUID id) {
        return paymentModeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment mode with id " + id + " was not found"));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " was not found"));
    }

    private DonationCampaign findDonationCampaign(UUID id) {
        return donationCampaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Donation campaign with id " + id + " was not found"));
    }

    private Specification<DonationTransaction> buildDonationTransactionSpecification(
        String search,
        UUID userId,
        UUID paymentModeId,
        UUID donationCampaignId,
        String userName,
        String userEmail,
        String paymentModeName,
        String donationCampaignTitle,
        BigDecimal minDonatedAmount,
        BigDecimal maxDonatedAmount
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            Join<DonationTransaction, User> userJoin = root.join("user");
            Join<DonationTransaction, PaymentMode> paymentModeJoin = root.join("paymentMode");
            Join<DonationTransaction, DonationCampaign> donationCampaignJoin = root.join("donationCampaign");

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), userId));
            }

            if (paymentModeId != null) {
                predicates.add(criteriaBuilder.equal(paymentModeJoin.get("id"), paymentModeId));
            }

            if (donationCampaignId != null) {
                predicates.add(criteriaBuilder.equal(donationCampaignJoin.get("id"), donationCampaignId));
            }

            addLikePredicate(predicates, criteriaBuilder, userJoin.get("email"), userEmail);
            addLikePredicate(predicates, criteriaBuilder, paymentModeJoin.get("name"), paymentModeName);
            addLikePredicate(predicates, criteriaBuilder, donationCampaignJoin.get("title"), donationCampaignTitle);

            String normalizedUserName = normalizeFilter(userName);
            if (normalizedUserName != null) {
                String pattern = "%" + normalizedUserName.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("middleName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("lastName")), pattern)
                    )
                );
            }

            if (minDonatedAmount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("donatedAmount"), minDonatedAmount));
            }

            if (maxDonatedAmount != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("donatedAmount"), maxDonatedAmount));
            }

            String normalizedSearch = normalizeFilter(search);
            if (normalizedSearch != null) {
                String pattern = "%" + normalizedSearch.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("middleName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("lastName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("email")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(paymentModeJoin.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(donationCampaignJoin.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("message")), pattern)
                    )
                );
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void applyRequest(DonationTransaction donationTransaction, DonationTransactionRequest request) {
        donationTransaction.setUser(findUser(request.userId()));
        donationTransaction.setPaymentMode(findPaymentMode(request.paymentModeId()));
        donationTransaction.setDonationCampaign(findDonationCampaign(request.donationCampaignId()));
        donationTransaction.setPhotoProof(request.photoProof().trim());
        donationTransaction.setDonatedAmount(request.donatedAmount());
        donationTransaction.setMessage(normalizeOptionalText(request.message()));
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
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

    private DonationTransactionResponse toResponse(DonationTransaction donationTransaction) {
        return new DonationTransactionResponse(
            donationTransaction.getId(),
            donationTransaction.getUser().getId(),
            buildUserFullName(donationTransaction.getUser()),
            donationTransaction.getUser().getEmail(),
            donationTransaction.getPaymentMode().getId(),
            donationTransaction.getPaymentMode().getName(),
            donationTransaction.getDonationCampaign().getId(),
            donationTransaction.getDonationCampaign().getTitle(),
            donationTransaction.getPhotoProof(),
            donationTransaction.getDonatedAmount(),
            donationTransaction.getMessage()
        );
    }

    private String buildUserFullName(User user) {
        String middleName = normalizeOptionalText(user.getMiddleName());

        if (middleName == null) {
            return user.getFirstName() + " " + user.getLastName();
        }

        return user.getFirstName() + " " + middleName + " " + user.getLastName();
    }
}
