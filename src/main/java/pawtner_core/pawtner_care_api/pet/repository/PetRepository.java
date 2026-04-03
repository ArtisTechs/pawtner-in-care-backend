package pawtner_core.pawtner_care_api.pet.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import pawtner_core.pawtner_care_api.pet.entity.Pet;

public interface PetRepository extends JpaRepository<Pet, UUID>, JpaSpecificationExecutor<Pet> {

    java.util.Optional<Pet> findByIdAndDeletedFalse(UUID id);
}

