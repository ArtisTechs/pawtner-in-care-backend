package pawtner_core.pawtner_care_api.support.dto;

import java.time.Instant;
import java.util.UUID;

import pawtner_core.pawtner_care_api.support.enums.SupportConversationStatus;

public record SupportConversationSummaryResponse(
    UUID id,
    UUID customerUserId,
    SupportConversationStatus status,
    Instant lastMessageAt,
    Instant customerLastReadAt,
    Instant createdAt,
    Instant updatedAt,
    long unreadCount
) {
}

