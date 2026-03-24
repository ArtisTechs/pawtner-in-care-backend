package pawtner_core.pawtner_care_api.support.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.dto.PageResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportConversationDetailsResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportConversationSummaryResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportMarkReadResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportMessageResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportSendMessageRequest;
import pawtner_core.pawtner_care_api.support.enums.SupportConversationStatus;
import pawtner_core.pawtner_care_api.support.service.SupportConversationService;
import pawtner_core.pawtner_care_api.support.service.SupportMessagingService;

@RestController
@RequestMapping("/api/support/admin")
public class AdminSupportController {

    private final SupportConversationService supportConversationService;
    private final SupportMessagingService supportMessagingService;

    public AdminSupportController(
        SupportConversationService supportConversationService,
        SupportMessagingService supportMessagingService
    ) {
        this.supportConversationService = supportConversationService;
        this.supportMessagingService = supportMessagingService;
    }

    @GetMapping("/conversations")
    public PageResponse<SupportConversationSummaryResponse> getConversations(
        @RequestParam UUID adminUserId,
        @RequestParam(required = false) UUID customerUserId,
        @RequestParam(required = false) SupportConversationStatus status,
        @RequestParam(defaultValue = "false") boolean unreadOnly,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "lastMessageAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(defaultValue = "false") boolean ignorePagination
    ) {
        return supportConversationService.getConversationsForAdmin(
            adminUserId,
            customerUserId,
            status,
            unreadOnly,
            page,
            size,
            sortBy,
            sortDir,
            ignorePagination
        );
    }

    @GetMapping("/conversations/{conversationId}")
    public SupportConversationDetailsResponse getConversation(
        @RequestParam UUID adminUserId,
        @PathVariable UUID conversationId
    ) {
        return supportConversationService.getConversationForAdmin(adminUserId, conversationId);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<SupportMessageResponse> getMessages(
        @RequestParam UUID adminUserId,
        @PathVariable UUID conversationId
    ) {
        return supportMessagingService.getMessagesForAdmin(adminUserId, conversationId);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public SupportMessageResponse sendMessage(
        @RequestParam UUID adminUserId,
        @PathVariable UUID conversationId,
        @Valid @RequestBody SupportSendMessageRequest request
    ) {
        return supportMessagingService.sendAdminMessage(adminUserId, conversationId, request.content());
    }

    @PostMapping("/conversations/{conversationId}/read")
    public SupportMarkReadResponse markMessagesAsRead(
        @RequestParam UUID adminUserId,
        @PathVariable UUID conversationId
    ) {
        return supportMessagingService.markAdminMessagesAsRead(adminUserId, conversationId);
    }
}
