package pawtner_core.pawtner_care_api.payment.service;

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
import pawtner_core.pawtner_care_api.payment.dto.PaymentModeRequest;
import pawtner_core.pawtner_care_api.payment.dto.PaymentModeResponse;
import pawtner_core.pawtner_care_api.payment.entity.PaymentMode;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.payment.repository.PaymentModeRepository;

@Service
public class PaymentModeService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "createdDate");

    private final PaymentModeRepository paymentModeRepository;

    public PaymentModeService(PaymentModeRepository paymentModeRepository) {
        this.paymentModeRepository = paymentModeRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentModeResponse> getPaymentModes(
        String search,
        String name,
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
        Specification<PaymentMode> specification = buildPaymentModeSpecification(search, name);

        if (ignorePagination) {
            List<PaymentModeResponse> content = paymentModeRepository.findAll(specification, sort).stream()
                .map(this::toResponse)
                .toList();
            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<PaymentModeResponse> responsePage = paymentModeRepository.findAll(specification, pageable).map(this::toResponse);
        return PageResponse.fromPage(responsePage, normalizedSortBy, direction.name().toLowerCase(), false);
    }

    @Transactional(readOnly = true)
    public PaymentModeResponse getPaymentMode(UUID id) {
        return toResponse(findPaymentMode(id));
    }

    @Transactional
    public PaymentModeResponse createPaymentMode(PaymentModeRequest request) {
        PaymentMode paymentMode = new PaymentMode();
        applyRequest(paymentMode, request);

        return toResponse(paymentModeRepository.save(paymentMode));
    }

    @Transactional
    public PaymentModeResponse updatePaymentMode(UUID id, PaymentModeRequest request) {
        PaymentMode paymentMode = findPaymentMode(id);
        applyRequest(paymentMode, request);

        return toResponse(paymentModeRepository.save(paymentMode));
    }

    @Transactional
    public void deletePaymentMode(UUID id) {
        PaymentMode paymentMode = findPaymentMode(id);
        paymentModeRepository.delete(paymentMode);
    }

    private PaymentMode findPaymentMode(UUID id) {
        return paymentModeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment mode with id " + id + " was not found"));
    }

    private Specification<PaymentMode> buildPaymentModeSpecification(String search, String name) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            addLikePredicate(predicates, criteriaBuilder, root.get("name"), name);

            String normalizedSearch = normalizeFilter(search);
            if (normalizedSearch != null) {
                String pattern = "%" + normalizedSearch.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void applyRequest(PaymentMode paymentMode, PaymentModeRequest request) {
        paymentMode.setName(request.name().trim());
        paymentMode.setAccountNumber(normalizeOptionalText(request.accountNumber()));
        paymentMode.setPhotoQr(normalizeOptionalText(request.photoQr()));
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

    private PaymentModeResponse toResponse(PaymentMode paymentMode) {
        return new PaymentModeResponse(
            paymentMode.getId(),
            paymentMode.getName(),
            paymentMode.getAccountNumber(),
            paymentMode.getPhotoQr(),
            paymentMode.getCreatedDate()
        );
    }
}

