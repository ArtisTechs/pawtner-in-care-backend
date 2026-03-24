package pawtner_core.pawtner_care_api.support.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.support.dto.SupportMarkReadResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportMessageResponse;
import pawtner_core.pawtner_care_api.support.entity.SupportConversation;
import pawtner_core.pawtner_care_api.support.entity.SupportMessage;
import pawtner_core.pawtner_care_api.support.entity.SupportMessageRead;
import pawtner_core.pawtner_care_api.support.enums.SupportConversationStatus;
import pawtner_core.pawtner_care_api.support.enums.SupportMessageType;
import pawtner_core.pawtner_care_api.support.enums.SupportParticipantRole;
import pawtner_core.pawtner_care_api.support.repository.SupportMessageReadRepository;
import pawtner_core.pawtner_care_api.support.repository.SupportMessageRepository;
import pawtner_core.pawtner_care_api.support.websocket.SupportMessagingPublisher;

@Service
public class SupportMessagingService {

    private final SupportConversationService supportConversationService;
    private final SupportMessageRepository supportMessageRepository;
    private final SupportMessageReadRepository supportMessageReadRepository;
    private final SupportUserAccessService supportUserAccessService;
    private final SupportMessagingPublisher supportMessagingPublisher;

    public SupportMessagingService(
        SupportConversationService supportConversationService,
        SupportMessageRepository supportMessageRepository,
        SupportMessageReadRepository supportMessageReadRepository,
        SupportUserAccessService supportUserAccessService,
        SupportMessagingPublisher supportMessagingPublisher
    ) {
        this.supportConversationService = supportConversationService;
        this.supportMessageRepository = supportMessageRepository;
        this.supportMessageReadRepository = supportMessageReadRepository;
        this.supportUserAccessService = supportUserAccessService;
        this.supportMessagingPublisher = supportMessagingPublisher;
    }

    @Transactional(readOnly = true)
    public List<SupportMessageResponse> getOwnMessages(UUID customerUserId) {
        supportUserAccessService.assertCustomer(customerUserId);
        SupportConversation conversation = supportConversationService.findExistingActiveConversation(customerUserId);
        if (conversation == null) {
            return List.of();
        }
        return getMessages(conversation.getId());
    }

    @Transactional(readOnly = true)
    public List<SupportMessageResponse> getMessagesForAdmin(UUID adminUserId, UUID conversationId) {
        supportUserAccessService.assertAdmin(adminUserId);
        supportConversationService.findConversation(conversationId);
        return getMessages(conversationId);
    }

    @Transactional
    public SupportMessageResponse sendCustomerMessage(UUID customerUserId, String content) {
        supportUserAccessService.assertCustomer(customerUserId);
        SupportConversation conversation = supportConversationService.findOrCreateActiveConversation(customerUserId);
        validateConversationIsOpen(conversation);

        SupportMessage savedMessage = supportMessageRepository.save(
            createMessage(conversation, customerUserId, SupportParticipantRole.CUSTOMER, content)
        );

        conversation.setLastMessageAt(savedMessage.getCreatedAt());
        supportConversationService.saveConversation(conversation);

        SupportMessageResponse response = toMessageResponse(savedMessage);
        supportMessagingPublisher.publishMessageCreated(
            supportConversationService.toConversationResponse(conversation),
            response
        );
        return response;
    }

    @Transactional
    public SupportMessageResponse sendAdminMessage(UUID adminUserId, UUID conversationId, String content) {
        supportUserAccessService.assertAdmin(adminUserId);
        SupportConversation conversation = supportConversationService.findConversation(conversationId);
        validateConversationIsOpen(conversation);

        SupportMessage savedMessage = supportMessageRepository.save(
            createMessage(conversation, adminUserId, SupportParticipantRole.ADMIN, content)
        );

        conversation.setLastMessageAt(savedMessage.getCreatedAt());
        supportConversationService.saveConversation(conversation);

        SupportMessageResponse response = toMessageResponse(savedMessage);
        supportMessagingPublisher.publishMessageCreated(
            supportConversationService.toConversationResponse(conversation),
            response
        );
        return response;
    }

    @Transactional
    public SupportMarkReadResponse markCustomerMessagesAsRead(UUID customerUserId) {
        supportUserAccessService.assertCustomer(customerUserId);
        SupportConversation conversation = supportConversationService.findOrCreateActiveConversation(customerUserId);
        Instant readAt = Instant.now();

        conversation.setCustomerLastReadAt(readAt);
        supportConversationService.saveConversation(conversation);

        long unreadCount = supportMessageRepository.countCustomerUnreadMessages(conversation.getId(), conversation.getCustomerLastReadAt());
        supportMessagingPublisher.publishMessagesRead(conversation.getId(), customerUserId, SupportParticipantRole.CUSTOMER, readAt);
        return new SupportMarkReadResponse(conversation.getId(), readAt, unreadCount);
    }

    @Transactional
    public SupportMarkReadResponse markAdminMessagesAsRead(UUID adminUserId, UUID conversationId) {
        supportUserAccessService.assertAdmin(adminUserId);
        SupportConversation conversation = supportConversationService.findConversation(conversationId);
        List<SupportMessage> unreadMessages = supportMessageRepository.findUnreadCustomerMessagesForAdmin(conversationId, adminUserId);
        Instant readAt = Instant.now();

        if (!unreadMessages.isEmpty()) {
            List<SupportMessageRead> reads = new ArrayList<>();
            for (SupportMessage unreadMessage : unreadMessages) {
                SupportMessageRead read = new SupportMessageRead();
                read.setMessage(unreadMessage);
                read.setAdminUserId(adminUserId);
                read.setReadAt(readAt);
                reads.add(read);
            }
            supportMessageReadRepository.saveAll(reads);
        }

        long unreadCount = supportMessageRepository.countAdminUnreadMessages(conversationId, adminUserId);
        supportMessagingPublisher.publishMessagesRead(conversation.getId(), adminUserId, SupportParticipantRole.ADMIN, readAt);
        return new SupportMarkReadResponse(conversationId, readAt, unreadCount);
    }

    public SupportMessageResponse toMessageResponse(SupportMessage message) {
        return new SupportMessageResponse(
            message.getId(),
            message.getConversation().getId(),
            message.getSenderUserId(),
            message.getSenderRole(),
            message.getContent(),
            message.getMessageType(),
            message.getCreatedAt()
        );
    }

    private List<SupportMessageResponse> getMessages(UUID conversationId) {
        return supportMessageRepository.findByConversation_IdOrderByCreatedAtAsc(conversationId).stream()
            .map(this::toMessageResponse)
            .toList();
    }

    private SupportMessage createMessage(
        SupportConversation conversation,
        UUID senderUserId,
        SupportParticipantRole senderRole,
        String content
    ) {
        SupportMessage message = new SupportMessage();
        message.setConversation(conversation);
        message.setSenderUserId(senderUserId);
        message.setSenderRole(senderRole);
        message.setContent(content.trim());
        message.setMessageType(SupportMessageType.TEXT);
        return message;
    }

    private void validateConversationIsOpen(SupportConversation conversation) {
        if (conversation.getStatus() != SupportConversationStatus.OPEN) {
            throw new IllegalArgumentException("Support conversation " + conversation.getId() + " is not open for new messages");
        }
    }
}

