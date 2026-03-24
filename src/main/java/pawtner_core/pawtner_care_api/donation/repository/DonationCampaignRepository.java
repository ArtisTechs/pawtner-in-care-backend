package pawtner_core.pawtner_care_api.donation.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import pawtner_core.pawtner_care_api.donation.entity.DonationCampaign;

public interface DonationCampaignRepository extends JpaRepository<DonationCampaign, UUID>, JpaSpecificationExecutor<DonationCampaign> {
}

