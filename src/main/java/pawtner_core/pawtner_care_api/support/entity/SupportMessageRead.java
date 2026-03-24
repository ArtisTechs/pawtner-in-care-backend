package pawtner_core.pawtner_care_api.support.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "support_message_reads",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_support_message_read_message_admin", columnNames = {"message_id", "admin_user_id"})
    },
    indexes = {
        @Index(name = "idx_support_message_read_admin", columnList = "admin_user_id"),
        @Index(name = "idx_support_message_read_message", columnList = "message_id")
    }
)
public class SupportMessageRead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, updatable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private SupportMessage message;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "admin_user_id", nullable = false, length = 36)
    private UUID adminUserId;

    @Column(name = "read_at", nullable = false)
    private Instant readAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public SupportMessage getMessage() {
        return message;
    }

    public void setMessage(SupportMessage message) {
        this.message = message;
    }

    public UUID getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(UUID adminUserId) {
        this.adminUserId = adminUserId;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    @PrePersist
    public void prePersist() {
        if (readAt == null) {
            readAt = Instant.now();
        }
    }
}
