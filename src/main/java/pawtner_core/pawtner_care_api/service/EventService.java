package pawtner_core.pawtner_care_api.service;

import java.time.LocalDate;
import java.time.LocalTime;
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

import pawtner_core.pawtner_care_api.dto.EventRequest;
import pawtner_core.pawtner_care_api.dto.EventResponse;
import pawtner_core.pawtner_care_api.dto.PageResponse;
import pawtner_core.pawtner_care_api.entity.Event;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.repository.EventRepository;

@Service
public class EventService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id",
        "title",
        "startDate",
        "endDate",
        "time",
        "createdDate",
        "updatedDate"
    );

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getEvents(
        String search,
        String title,
        LocalDate date,
        LocalDate startDateFrom,
        LocalDate startDateTo,
        LocalDate endDateFrom,
        LocalDate endDateTo,
        LocalTime timeFrom,
        LocalTime timeTo,
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
        Sort sort = buildSort(normalizedSortBy, direction);
        Specification<Event> specification = buildEventSpecification(
            search,
            title,
            date,
            startDateFrom,
            startDateTo,
            endDateFrom,
            endDateTo,
            timeFrom,
            timeTo
        );

        if (ignorePagination) {
            List<EventResponse> content = eventRepository.findAll(specification, sort).stream()
                .map(this::toResponse)
                .toList();
            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<EventResponse> responsePage = eventRepository.findAll(specification, pageable).map(this::toResponse);
        return PageResponse.fromPage(responsePage, normalizedSortBy, direction.name().toLowerCase(), false);
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID id) {
        return toResponse(findEvent(id));
    }

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        validateDates(request);

        Event event = new Event();
        applyRequest(event, request);

        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse updateEvent(UUID id, EventRequest request) {
        validateDates(request);

        Event event = findEvent(id);
        applyRequest(event, request);

        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(UUID id) {
        Event event = findEvent(id);
        eventRepository.delete(event);
    }

    private Event findEvent(UUID id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event with id " + id + " was not found"));
    }

    private Specification<Event> buildEventSpecification(
        String search,
        String title,
        LocalDate date,
        LocalDate startDateFrom,
        LocalDate startDateTo,
        LocalDate endDateFrom,
        LocalDate endDateTo,
        LocalTime timeFrom,
        LocalTime timeTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            addLikePredicate(predicates, criteriaBuilder, root.get("title"), title);
            addFromDatePredicate(predicates, criteriaBuilder, root.get("startDate"), startDateFrom);
            addToDatePredicate(predicates, criteriaBuilder, root.get("startDate"), startDateTo);
            addFromDatePredicate(predicates, criteriaBuilder, root.get("endDate"), endDateFrom);
            addToDatePredicate(predicates, criteriaBuilder, root.get("endDate"), endDateTo);
            addFromTimePredicate(predicates, criteriaBuilder, root.get("time"), timeFrom);
            addToTimePredicate(predicates, criteriaBuilder, root.get("time"), timeTo);

            if (date != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), date));
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), date));
            }

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

    private void applyRequest(Event event, EventRequest request) {
        event.setTitle(request.title().trim());
        event.setDescription(normalizeOptionalText(request.description()));
        event.setStartDate(request.startDate());
        event.setEndDate(request.endDate());
        event.setTime(request.time());
        event.setLink(normalizeOptionalText(request.link()));
        event.setPhoto(normalizeOptionalText(request.photo()));
    }

    private void validateDates(EventRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("End date cannot be earlier than start date");
        }
    }

    private Sort buildSort(String sortBy, Sort.Direction direction) {
        if ("startDate".equals(sortBy)) {
            return Sort.by(new Sort.Order(direction, "startDate"), new Sort.Order(direction, "time"));
        }

        return Sort.by(direction, sortBy);
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

    private void addFromTimePredicate(
        List<Predicate> predicates,
        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
        jakarta.persistence.criteria.Path<LocalTime> path,
        LocalTime value
    ) {
        if (value != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path, value));
        }
    }

    private void addToTimePredicate(
        List<Predicate> predicates,
        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
        jakarta.persistence.criteria.Path<LocalTime> path,
        LocalTime value
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
            return "startDate";
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

    private EventResponse toResponse(Event event) {
        return new EventResponse(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getStartDate(),
            event.getEndDate(),
            event.getTime(),
            event.getLink(),
            event.getPhoto(),
            event.getCreatedDate(),
            event.getUpdatedDate()
        );
    }
}
