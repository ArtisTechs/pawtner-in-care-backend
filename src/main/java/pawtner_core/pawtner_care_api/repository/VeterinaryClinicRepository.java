package pawtner_core.pawtner_care_api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import pawtner_core.pawtner_care_api.entity.VeterinaryClinic;

public interface VeterinaryClinicRepository extends JpaRepository<VeterinaryClinic, UUID>, JpaSpecificationExecutor<VeterinaryClinic> {
}
