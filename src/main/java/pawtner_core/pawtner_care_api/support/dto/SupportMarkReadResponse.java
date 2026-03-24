package pawtner_core.pawtner_care_api.support.dto;

import java.time.Instant;
import java.util.UUID;

public record SupportMarkReadResponse(
    UUID conversationId,
    Instant readAt,
    long unreadCount
) {
}
