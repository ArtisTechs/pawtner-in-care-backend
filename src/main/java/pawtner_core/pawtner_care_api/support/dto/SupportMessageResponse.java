package pawtner_core.pawtner_care_api.support.dto;

import java.time.Instant;
import java.util.UUID;

import pawtner_core.pawtner_care_api.support.enums.SupportMessageType;
import pawtner_core.pawtner_care_api.support.enums.SupportParticipantRole;

public record SupportMessageResponse(
    UUID id,
    UUID conversationId,
    UUID senderUserId,
    SupportParticipantRole senderRole,
    String content,
    SupportMessageType messageType,
    Instant createdAt
) {
}

