package pawtner_core.pawtner_care_api.gamification.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 36)
    private UUID userId;

    @Column(nullable = false)
    private Long totalAdoptedPets;

    @Column(nullable = false)
    private Long totalDonations;

    @Column(nullable = false)
    private Long monthsActive;

    @Column(nullable = false)
    private Boolean isRegistered;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (totalAdoptedPets == null) {
            totalAdoptedPets = 0L;
        }

        if (totalDonations == null) {
            totalDonations = 0L;
        }

        if (monthsActive == null) {
            monthsActive = 0L;
        }

        if (isRegistered == null) {
            isRegistered = Boolean.FALSE;
        }

        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Long getTotalAdoptedPets() {
        return totalAdoptedPets;
    }

    public void setTotalAdoptedPets(Long totalAdoptedPets) {
        this.totalAdoptedPets = totalAdoptedPets;
    }

    public Long getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(Long totalDonations) {
        this.totalDonations = totalDonations;
    }

    public Long getMonthsActive() {
        return monthsActive;
    }

    public void setMonthsActive(Long monthsActive) {
        this.monthsActive = monthsActive;
    }

    public Boolean getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(Boolean registered) {
        isRegistered = registered;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

