package pawtner_core.pawtner_care_api.user.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.user.dto.UserDetailResponse;
import pawtner_core.pawtner_care_api.user.dto.UserRequest;
import pawtner_core.pawtner_care_api.user.dto.UserResponse;
import pawtner_core.pawtner_care_api.user.entity.User;
import pawtner_core.pawtner_care_api.user.enums.UserRole;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.gamification.service.AchievementService;
import pawtner_core.pawtner_care_api.user.repository.UserRepository;

@Service
public class UserService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "firstName", "middleName", "lastName", "email", "profilePicture");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AchievementService achievementService;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AchievementService achievementService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.achievementService = achievementService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(
        String search,
        String firstName,
        String middleName,
        String lastName,
        String email,
        String profilePicture,
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
        Specification<User> specification = buildUserSpecification(search, firstName, middleName, lastName, email, profilePicture);

        if (ignorePagination) {
            List<UserResponse> content = userRepository.findAll(specification, sort).stream()
                .map(this::toResponse)
                .toList();
            return PageResponse.fromList(content, normalizedSortBy, direction.name().toLowerCase(), true);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<User> users = userRepository.findAll(specification, pageable);
        Page<UserResponse> responsePage = users.map(this::toResponse);

        return PageResponse.fromPage(responsePage, normalizedSortBy, direction.name().toLowerCase(), false);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUser(UUID id) {
        User user = findUser(id);
        return toDetailResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        validateEmailAvailability(normalizedEmail);

        User user = new User();
        applyRequest(user, request, normalizedEmail);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserRequest request) {
        User user = findUser(id);
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new IllegalArgumentException("Email is already in use");
        }

        applyRequest(user, request, normalizedEmail);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = findUser(id);
        userRepository.delete(user);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " was not found"));
    }

    private Specification<User> buildUserSpecification(
        String search,
        String firstName,
        String middleName,
        String lastName,
        String email,
        String profilePicture
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            addLikePredicate(predicates, criteriaBuilder, root.get("firstName"), firstName);
            addLikePredicate(predicates, criteriaBuilder, root.get("middleName"), middleName);
            addLikePredicate(predicates, criteriaBuilder, root.get("lastName"), lastName);
            addLikePredicate(predicates, criteriaBuilder, root.get("email"), email);
            addLikePredicate(predicates, criteriaBuilder, root.get("profilePicture"), profilePicture);

            String normalizedSearch = normalizeFilter(search);
            if (normalizedSearch != null) {
                String pattern = "%" + normalizedSearch.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("middleName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("profilePicture")), pattern)
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

    private void validateEmailAvailability(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }
    }

    private void applyRequest(User user, UserRequest request, String normalizedEmail) {
        user.setFirstName(request.firstName().trim());
        user.setMiddleName(normalizeOptionalText(request.middleName()));
        user.setLastName(request.lastName().trim());
        user.setEmail(normalizedEmail);
        user.setProfilePicture(normalizeOptionalText(request.profilePicture()));
        user.setPassword(passwordEncoder.encode(request.password().trim()));
        user.setRole(resolveRole(request.role()));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private UserRole resolveRole(UserRole role) {
        return role == null ? UserRole.USER : role;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getMiddleName(),
            user.getLastName(),
            user.getEmail(),
            user.getProfilePicture(),
            user.getRole()
        );
    }

    private UserDetailResponse toDetailResponse(User user) {
        return new UserDetailResponse(
            user.getId(),
            user.getFirstName(),
            user.getMiddleName(),
            user.getLastName(),
            user.getEmail(),
            user.getProfilePicture(),
            user.getRole(),
            achievementService.getUserAchievements(user.getId())
        );
    }
}

