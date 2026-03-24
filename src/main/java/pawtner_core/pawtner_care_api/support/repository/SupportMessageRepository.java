package pawtner_core.pawtner_care_api.support.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pawtner_core.pawtner_care_api.support.entity.SupportMessage;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, UUID> {

    List<SupportMessage> findByConversation_IdOrderByCreatedAtAsc(UUID conversationId);

    @Query("""
        select count(m)
        from SupportMessage m
        where m.conversation.id = :conversationId
        and m.senderRole = pawtner_core.pawtner_care_api.support.enums.SupportParticipantRole.ADMIN
        and (:customerLastReadAt is null or m.createdAt > :customerLastReadAt)
        """)
    long countCustomerUnreadMessages(
        @Param("conversationId") UUID conversationId,
        @Param("customerLastReadAt") Instant customerLastReadAt
    );

    @Query("""
        select count(m)
        from SupportMessage m
        where m.conversation.id = :conversationId
        and m.senderRole = pawtner_core.pawtner_care_api.support.enums.SupportParticipantRole.CUSTOMER
        and not exists (
            select 1
            from SupportMessageRead r
            where r.message = m
            and r.adminUserId = :adminUserId
        )
        """)
    long countAdminUnreadMessages(
        @Param("conversationId") UUID conversationId,
        @Param("adminUserId") UUID adminUserId
    );

    @Query("""
        select m
        from SupportMessage m
        where m.conversation.id = :conversationId
        and m.senderRole = pawtner_core.pawtner_care_api.support.enums.SupportParticipantRole.CUSTOMER
        and not exists (
            select 1
            from SupportMessageRead r
            where r.message = m
            and r.adminUserId = :adminUserId
        )
        order by m.createdAt asc
        """)
    List<SupportMessage> findUnreadCustomerMessagesForAdmin(
        @Param("conversationId") UUID conversationId,
        @Param("adminUserId") UUID adminUserId
    );
}
