package pawtner_core.pawtner_care_api.support.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.support.dto.SupportConversationDetailsResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportMarkReadResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportMessageResponse;
import pawtner_core.pawtner_care_api.support.dto.SupportSendMessageRequest;
import pawtner_core.pawtner_care_api.support.service.SupportConversationService;
import pawtner_core.pawtner_care_api.support.service.SupportMessagingService;

@RestController
@RequestMapping("/api/support/customer")
public class CustomerSupportController {

    private final SupportConversationService supportConversationService;
    private final SupportMessagingService supportMessagingService;

    public CustomerSupportController(
        SupportConversationService supportConversationService,
        SupportMessagingService supportMessagingService
    ) {
        this.supportConversationService = supportConversationService;
        this.supportMessagingService = supportMessagingService;
    }

    @GetMapping("/conversation")
    public SupportConversationDetailsResponse getOwnConversation(@RequestParam UUID customerUserId) {
        return supportConversationService.getOwnConversation(customerUserId);
    }

    @GetMapping("/messages")
    public List<SupportMessageResponse> getOwnMessages(@RequestParam UUID customerUserId) {
        return supportMessagingService.getOwnMessages(customerUserId);
    }

    @PostMapping("/messages")
    public SupportMessageResponse sendMessage(
        @RequestParam UUID customerUserId,
        @Valid @RequestBody SupportSendMessageRequest request
    ) {
        return supportMessagingService.sendCustomerMessage(customerUserId, request.content());
    }

    @PostMapping("/read")
    public SupportMarkReadResponse markMessagesAsRead(@RequestParam UUID customerUserId) {
        return supportMessagingService.markCustomerMessagesAsRead(customerUserId);
    }
}
