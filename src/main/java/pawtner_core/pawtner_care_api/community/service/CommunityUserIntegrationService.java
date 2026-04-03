package pawtner_core.pawtner_care_api.community.service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import pawtner_core.pawtner_care_api.community.dto.CommunityUserSummaryResponse;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.user.entity.User;
import pawtner_core.pawtner_care_api.user.repository.UserRepository;

@Service
public class CommunityUserIntegrationService {

    private final UserRepository userRepository;

    public CommunityUserIntegrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with id " + userId + " was not found");
        }
    }

    public CommunityUserSummaryResponse getUserSummary(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " was not found"));
        return toUserSummary(user);
    }

    public Map<UUID, CommunityUserSummaryResponse> getUserSummaries(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, CommunityUserSummaryResponse> summariesById = userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, this::toUserSummary));

        userIds.stream()
            .filter(userId -> !summariesById.containsKey(userId))
            .findFirst()
            .ifPresent(missingUserId -> {
                throw new ResourceNotFoundException("User with id " + missingUserId + " was not found");
            });

        return summariesById;
    }

    private CommunityUserSummaryResponse toUserSummary(User user) {
        return new CommunityUserSummaryResponse(
            user.getId(),
            user.getFirstName(),
            user.getMiddleName(),
            user.getLastName(),
            user.getProfilePicture()
        );
    }
}

