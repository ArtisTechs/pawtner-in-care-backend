package pawtner_core.pawtner_care_api.auth.dto;

import pawtner_core.pawtner_care_api.user.dto.UserResponse;

public record AuthResponse(
    String tokenType,
    String accessToken,
    UserResponse user
) {
}

