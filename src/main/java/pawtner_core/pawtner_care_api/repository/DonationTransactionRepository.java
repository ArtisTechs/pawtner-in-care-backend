package pawtner_core.pawtner_care_api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pawtner_core.pawtner_care_api.entity.DonationTransaction;

public interface DonationTransactionRepository
    extends JpaRepository<DonationTransaction, UUID>, JpaSpecificationExecutor<DonationTransaction> {

    @Query("""
        select coalesce(sum(dt.donatedAmount), 0)
        from DonationTransaction dt
        where dt.donationCampaign.id = :donationCampaignId
    """)
    java.math.BigDecimal sumDonatedAmountByDonationCampaignId(@Param("donationCampaignId") UUID donationCampaignId);
}
