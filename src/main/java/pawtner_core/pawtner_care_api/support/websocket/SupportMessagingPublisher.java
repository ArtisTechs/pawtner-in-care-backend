package pawtner_core.pawtner_care_api.support.websocket;

import java.time.Instant;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import pawtner_core.pawtner_care_api.support.dto.SupportConversationResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportMessageResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportRealtimeMessagePayload;
import pawtner_core.pawtner_care_api.support.dto.SupportRealtimeReadPayload;
import pawtner_core.pawtner_care_api.support.enums.SupportParticipantRole;

@Component
public class SupportMessagingPublisher {

    private static final String MESSAGE_CREATED_EVENT = "MESSAGE_CREATED";
    private static final String MESSAGE_READ_EVENT = "MESSAGES_READ";

    private final SimpMessagingTemplate simpMessagingTemplate;

    public SupportMessagingPublisher(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void publishMessageCreated(
        SupportConversationResponse conversation,
        SupportMessageResponse message
    ) {
        SupportRealtimeMessagePayload payload = new SupportRealtimeMessagePayload(
            MESSAGE_CREATED_EVENT,
            conversation,
            message
        );

        UUID conversationId = message.conversationId();
        simpMessagingTemplate.convertAndSend("/topic/support/conversations/" + conversationId, payload);
        simpMessagingTemplate.convertAndSend("/topic/support/customers/" + conversation.customerUserId(), payload);
        simpMessagingTemplate.convertAndSend("/topic/support/admin/conversations/" + conversationId, payload);
    }

    public void publishMessagesRead(
        UUID conversationId,
        UUID actorUserId,
        SupportParticipantRole actorRole,
        Instant readAt
    ) {
        SupportRealtimeReadPayload payload = new SupportRealtimeReadPayload(
            MESSAGE_READ_EVENT,
            conversationId,
            actorUserId,
            actorRole,
            readAt
        );

        simpMessagingTemplate.convertAndSend("/topic/support/conversations/" + conversationId + "/reads", payload);
        simpMessagingTemplate.convertAndSend("/topic/support/admin/conversations/" + conversationId + "/reads", payload);
    }
}

