package pawtner_core.pawtner_care_api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.entity.PaymentMode;

public interface PaymentModeRepository extends JpaRepository<PaymentMode, UUID> {
}
