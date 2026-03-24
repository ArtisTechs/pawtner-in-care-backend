package pawtner_core.pawtner_care_api.emergency.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import pawtner_core.pawtner_care_api.emergency.entity.EmergencySos;

public interface EmergencySosRepository extends JpaRepository<EmergencySos, UUID>, JpaSpecificationExecutor<EmergencySos> {
}

