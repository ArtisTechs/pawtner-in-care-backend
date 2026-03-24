package pawtner_core.pawtner_care_api.support.dto;

import java.time.Instant;
import java.util.UUID;

import pawtner_core.pawtner_care_api.support.enums.SupportParticipantRole;

public record SupportRealtimeReadPayload(
    String eventType,
    UUID conversationId,
    UUID actorUserId,
    SupportParticipantRole actorRole,
    Instant readAt
) {
}
