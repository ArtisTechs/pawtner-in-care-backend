package pawtner_core.pawtner_care_api.dto;

public record AuthResponse(
    String tokenType,
    String accessToken,
    UserResponse user
) {
}
