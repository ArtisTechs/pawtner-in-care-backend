package pawtner_core.pawtner_care_api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.entity.Pet;

public interface PetRepository extends JpaRepository<Pet, UUID> {
}
