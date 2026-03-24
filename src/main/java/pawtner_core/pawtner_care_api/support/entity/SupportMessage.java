package pawtner_core.pawtner_care_api.support.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import pawtner_core.pawtner_care_api.support.enums.SupportMessageType;
import pawtner_core.pawtner_care_api.support.enums.SupportParticipantRole;

@Entity
@Table(
    name = "support_messages",
    indexes = {
        @Index(name = "idx_support_message_conversation", columnList = "conversation_id"),
        @Index(name = "idx_support_message_sender", columnList = "sender_user_id"),
        @Index(name = "idx_support_message_created_at", columnList = "created_at")
    }
)
public class SupportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, updatable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private SupportConversation conversation;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "sender_user_id", nullable = false, length = 36)
    private UUID senderUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false, length = 20)
    private SupportParticipantRole senderRole;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private SupportMessageType messageType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public SupportConversation getConversation() {
        return conversation;
    }

    public void setConversation(SupportConversation conversation) {
        this.conversation = conversation;
    }

    public UUID getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(UUID senderUserId) {
        this.senderUserId = senderUserId;
    }

    public SupportParticipantRole getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(SupportParticipantRole senderRole) {
        this.senderRole = senderRole;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SupportMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(SupportMessageType messageType) {
        this.messageType = messageType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (messageType == null) {
            messageType = SupportMessageType.TEXT;
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

