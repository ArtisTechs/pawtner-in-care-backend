package pawtner_core.pawtner_care_api.pet.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.pet.entity.PetFavorite;

public interface PetFavoriteRepository extends JpaRepository<PetFavorite, UUID> {

    boolean existsByPetIdAndUserId(UUID petId, UUID userId);

    Optional<PetFavorite> findByPetIdAndUserId(UUID petId, UUID userId);

    List<PetFavorite> findByUserIdAndPetDeletedFalseOrderByCreatedAtDesc(UUID userId);

    void deleteByPetId(UUID petId);
}
