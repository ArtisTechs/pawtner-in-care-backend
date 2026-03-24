package pawtner_core.pawtner_care_api.support.dto;

public record SupportConversationDetailsResponse(
    SupportConversationResponse conversation,
    long unreadCount
) {
}
