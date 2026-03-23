package pawtner_core.pawtner_care_api.service;

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

import pawtner_core.pawtner_care_api.dto.PageResponse;
import pawtner_core.pawtner_care_api.dto.VeterinaryClinicRequest;
import pawtner_core.pawtner_care_api.dto.VeterinaryClinicResponse;
import pawtner_core.pawtner_care_api.entity.VeterinaryClinic;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.repository.VeterinaryClinicRepository;

@Service
public class VeterinaryClinicService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id",
        "name",
        "locationAddress",
        "longitude",
        "latitude",
        "openingTime",
        "ratings",
        "createdDate",
        "updatedDate"
    );

    private final VeterinaryClinicRepository veterinaryClinicRepository;

    public VeterinaryClinicService(VeterinaryClinicRepository veterinaryClinicRepository) {
        this.veterinaryClinicRepository = veterinaryClinicRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<VeterinaryClinicResponse> getVeterinaryClinics(
        String search,
        String name,
        String locationAddress,
        String ratings,
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
        Specification<VeterinaryClinic> specification = buildVeterinaryClinicSpecification(search, name, locationAddress, ratings);

        if (ignorePagination) {
            List<VeterinaryClinicResponse> content = veterinaryClinicRepository.findAll(specification, sort).stream()
                .map(this::toResponse)
                .toList();
            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<VeterinaryClinicResponse> responsePage = veterinaryClinicRepository.findAll(specification, pageable).map(this::toResponse);
        return PageResponse.fromPage(responsePage, normalizedSortBy, direction.name().toLowerCase(), false);
    }

    @Transactional(readOnly = true)
    public VeterinaryClinicResponse getVeterinaryClinic(UUID id) {
        return toResponse(findVeterinaryClinic(id));
    }

    @Transactional
    public VeterinaryClinicResponse createVeterinaryClinic(VeterinaryClinicRequest request) {
        VeterinaryClinic veterinaryClinic = new VeterinaryClinic();
        applyRequest(veterinaryClinic, request);

        return toResponse(veterinaryClinicRepository.save(veterinaryClinic));
    }

    @Transactional
    public VeterinaryClinicResponse updateVeterinaryClinic(UUID id, VeterinaryClinicRequest request) {
        VeterinaryClinic veterinaryClinic = findVeterinaryClinic(id);
        applyRequest(veterinaryClinic, request);

        return toResponse(veterinaryClinicRepository.save(veterinaryClinic));
    }

    @Transactional
    public void deleteVeterinaryClinic(UUID id) {
        VeterinaryClinic veterinaryClinic = findVeterinaryClinic(id);
        veterinaryClinicRepository.delete(veterinaryClinic);
    }

    private VeterinaryClinic findVeterinaryClinic(UUID id) {
        return veterinaryClinicRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Veterinary clinic with id " + id + " was not found"));
    }

    private Specification<VeterinaryClinic> buildVeterinaryClinicSpecification(
        String search,
        String name,
        String locationAddress,
        String ratings
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            addLikePredicate(predicates, criteriaBuilder, root.get("name"), name);
            addLikePredicate(predicates, criteriaBuilder, root.get("locationAddress"), locationAddress);
            addLikePredicate(predicates, criteriaBuilder, root.get("ratings"), ratings);

            String normalizedSearch = normalizeFilter(search);
            if (normalizedSearch != null) {
                String pattern = "%" + normalizedSearch.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("locationAddress")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("ratings")), pattern)
                    )
                );
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void applyRequest(VeterinaryClinic veterinaryClinic, VeterinaryClinicRequest request) {
        veterinaryClinic.setName(request.name().trim());
        veterinaryClinic.setDescription(normalizeOptionalText(request.description()));
        veterinaryClinic.setLocationAddress(request.locationAddress().trim());
        veterinaryClinic.setLongitude(request.longitude());
        veterinaryClinic.setLatitude(request.latitude());
        veterinaryClinic.setOpeningTime(request.openingTime());
        veterinaryClinic.setContactNumbers(normalizeStringList(request.contactNumbers()));
        veterinaryClinic.setRatings(normalizeOptionalText(request.ratings()));
        veterinaryClinic.setPhotos(normalizeStringList(request.photos()));
        veterinaryClinic.setLogo(normalizeOptionalText(request.logo()));
        veterinaryClinic.setVideos(normalizeStringList(request.videos()));
    }

    private List<String> normalizeStringList(List<String> values) {
        if (values == null) {
            return new java.util.ArrayList<>();
        }

        return new java.util.ArrayList<>(values.stream()
            .filter(java.util.Objects::nonNull)
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .toList());
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
            return "name";
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

    private VeterinaryClinicResponse toResponse(VeterinaryClinic veterinaryClinic) {
        return new VeterinaryClinicResponse(
            veterinaryClinic.getId(),
            veterinaryClinic.getName(),
            veterinaryClinic.getDescription(),
            veterinaryClinic.getLocationAddress(),
            veterinaryClinic.getLongitude(),
            veterinaryClinic.getLatitude(),
            veterinaryClinic.getOpeningTime(),
            List.copyOf(veterinaryClinic.getContactNumbers()),
            veterinaryClinic.getRatings(),
            veterinaryClinic.getUpdatedDate(),
            veterinaryClinic.getCreatedDate(),
            List.copyOf(veterinaryClinic.getPhotos()),
            veterinaryClinic.getLogo(),
            List.copyOf(veterinaryClinic.getVideos())
        );
    }
}
