package pawtner_core.pawtner_care_api.gamification.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import pawtner_core.pawtner_care_api.user.entity.User;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.gamification.dto.GamificationUserProfileResponse;
import pawtner_core.pawtner_care_api.user.repository.UserRepository;

@Service
public class ExistingUserGamificationProfileService implements GamificationUserProfileService {

    private final UserRepository userRepository;

    public ExistingUserGamificationProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public GamificationUserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " was not found"));

        String displayName = (user.getFirstName() + " " + user.getLastName()).trim();
        return new GamificationUserProfileResponse(user.getId(), displayName, user.getEmail());
    }
}

