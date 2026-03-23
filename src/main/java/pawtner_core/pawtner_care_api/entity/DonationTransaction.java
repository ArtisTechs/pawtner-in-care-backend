package pawtner_core.pawtner_care_api.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "donation_transactions")
public class DonationTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, updatable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_mode_id", nullable = false)
    private PaymentMode paymentMode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donation_campaign_id", nullable = false)
    private DonationCampaign donationCampaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "photo_proof", nullable = false, length = 500)
    private String photoProof;

    @Column(name = "donated_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal donatedAmount;

    @Column(columnDefinition = "TEXT")
    private String message;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public DonationCampaign getDonationCampaign() {
        return donationCampaign;
    }

    public void setDonationCampaign(DonationCampaign donationCampaign) {
        this.donationCampaign = donationCampaign;
    }

    public String getPhotoProof() {
        return photoProof;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPhotoProof(String photoProof) {
        this.photoProof = photoProof;
    }

    public BigDecimal getDonatedAmount() {
        return donatedAmount;
    }

    public void setDonatedAmount(BigDecimal donatedAmount) {
        this.donatedAmount = donatedAmount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
