package pawtner_core.pawtner_care_api.support.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import pawtner_core.pawtner_care_api.support.enums.SupportConversationStatus;

@Entity
@Table(
    name = "support_conversations",
    indexes = {
        @Index(name = "idx_support_conversation_customer", columnList = "customer_user_id"),
        @Index(name = "idx_support_conversation_status", columnList = "status"),
        @Index(name = "idx_support_conversation_last_message_at", columnList = "last_message_at")
    }
)
public class SupportConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, updatable = false, length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "customer_user_id", nullable = false, length = 36)
    private UUID customerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupportConversationStatus status;

    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;

    @Column(name = "customer_last_read_at")
    private Instant customerLastReadAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCustomerUserId() {
        return customerUserId;
    }

    public void setCustomerUserId(UUID customerUserId) {
        this.customerUserId = customerUserId;
    }

    public SupportConversationStatus getStatus() {
        return status;
    }

    public void setStatus(SupportConversationStatus status) {
        this.status = status;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Instant getCustomerLastReadAt() {
        return customerLastReadAt;
    }

    public void setCustomerLastReadAt(Instant customerLastReadAt) {
        this.customerLastReadAt = customerLastReadAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (status == null) {
            status = SupportConversationStatus.OPEN;
        }
        if (lastMessageAt == null) {
            lastMessageAt = now;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
