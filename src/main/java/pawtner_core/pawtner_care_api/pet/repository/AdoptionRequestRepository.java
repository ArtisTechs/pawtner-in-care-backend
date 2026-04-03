package pawtner_core.pawtner_care_api.pet.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import pawtner_core.pawtner_care_api.pet.entity.AdoptionRequest;
import pawtner_core.pawtner_care_api.pet.enums.AdoptionRequestStatus;

public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, UUID>, JpaSpecificationExecutor<AdoptionRequest> {

    List<AdoptionRequest> findAllByOrderByCreatedAtAsc();

    List<AdoptionRequest> findByPetIdOrderByCreatedAtDesc(UUID petId);

    List<AdoptionRequest> findByRequesterIdOrderByCreatedAtDesc(UUID requesterId);

    Optional<AdoptionRequest> findByPetIdAndRequesterId(UUID petId, UUID requesterId);

    boolean existsByPetId(UUID petId);

    boolean existsByPetIdAndRequesterIdAndStatus(UUID petId, UUID requesterId, AdoptionRequestStatus status);

    List<AdoptionRequest> findByPetIdAndStatus(UUID petId, AdoptionRequestStatus status);
}
