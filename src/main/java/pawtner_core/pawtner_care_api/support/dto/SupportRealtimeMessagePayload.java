package pawtner_core.pawtner_care_api.support.dto;

public record SupportRealtimeMessagePayload(
    String eventType,
    SupportConversationResponse conversation,
    SupportMessageResponse message
) {
}
