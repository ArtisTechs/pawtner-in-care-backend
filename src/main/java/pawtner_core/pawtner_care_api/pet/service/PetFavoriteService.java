package pawtner_core.pawtner_care_api.pet.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.pet.dto.PetFavoriteResponse;
import pawtner_core.pawtner_care_api.pet.dto.PetResponse;
import pawtner_core.pawtner_care_api.pet.entity.PetFavorite;
import pawtner_core.pawtner_care_api.pet.repository.PetFavoriteRepository;

@Service
public class PetFavoriteService {

    private final PetFavoriteRepository petFavoriteRepository;
    private final PetService petService;

    public PetFavoriteService(PetFavoriteRepository petFavoriteRepository, PetService petService) {
        this.petFavoriteRepository = petFavoriteRepository;
        this.petService = petService;
    }

    @Transactional
    public PetFavoriteResponse addFavorite(UUID petId, UUID userId) {
        if (petFavoriteRepository.existsByPetIdAndUserId(petId, userId)) {
            return new PetFavoriteResponse(petId, userId, true);
        }

        PetFavorite petFavorite = new PetFavorite();
        petFavorite.setPet(petService.findPetEntity(petId));
        petFavorite.setUser(petService.findUserEntity(userId));
        petFavoriteRepository.save(petFavorite);

        return new PetFavoriteResponse(petId, userId, true);
    }

    @Transactional
    public PetFavoriteResponse removeFavorite(UUID petId, UUID userId) {
        petService.findPetEntity(petId);
        petService.findUserEntity(userId);

        petFavoriteRepository.findByPetIdAndUserId(petId, userId)
            .ifPresent(petFavoriteRepository::delete);

        return new PetFavoriteResponse(petId, userId, false);
    }

    @Transactional(readOnly = true)
    public List<PetResponse> getFavoritePetsByUser(UUID userId) {
        petService.findUserEntity(userId);

        return petFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(PetFavorite::getPet)
            .map(petService::toPetResponse)
            .toList();
    }
}
