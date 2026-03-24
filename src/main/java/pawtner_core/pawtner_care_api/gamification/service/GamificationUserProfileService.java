package pawtner_core.pawtner_care_api.gamification.service;

import java.util.UUID;

import pawtner_core.pawtner_care_api.gamification.dto.GamificationUserProfileResponse;

public interface GamificationUserProfileService {

    GamificationUserProfileResponse getProfile(UUID userId);
}

