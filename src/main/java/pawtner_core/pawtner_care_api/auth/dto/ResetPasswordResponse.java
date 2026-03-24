package pawtner_core.pawtner_care_api.auth.dto;

public record ResetPasswordResponse(
    String email,
    String message
) {
}
