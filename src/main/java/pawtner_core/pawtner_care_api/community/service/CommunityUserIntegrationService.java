package pawtner_core.pawtner_care_api.community.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.repository.UserRepository;

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
}
