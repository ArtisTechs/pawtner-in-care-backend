package pawtner_core.pawtner_care_api.support.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.support.entity.SupportMessageRead;

public interface SupportMessageReadRepository extends JpaRepository<SupportMessageRead, UUID> {
}
