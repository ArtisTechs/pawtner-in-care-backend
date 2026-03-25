package pawtner_core.pawtner_care_api.pet.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.pet.dto.PetFavoriteResponse;
import pawtner_core.pawtner_care_api.pet.dto.PetResponse;
import pawtner_core.pawtner_care_api.pet.service.PetFavoriteService;

@RestController
public class PetFavoriteController {

    private final PetFavoriteService petFavoriteService;

    public PetFavoriteController(PetFavoriteService petFavoriteService) {
        this.petFavoriteService = petFavoriteService;
    }

    @PostMapping("/api/pets/{petId}/favorites")
    public ResponseEntity<PetFavoriteResponse> addFavorite(
        @PathVariable UUID petId,
        @RequestHeader("X-User-Id") UUID currentUserId
    ) {
        return ResponseEntity.ok(petFavoriteService.addFavorite(petId, currentUserId));
    }

    @DeleteMapping("/api/pets/{petId}/favorites")
    public ResponseEntity<PetFavoriteResponse> removeFavorite(
        @PathVariable UUID petId,
        @RequestHeader("X-User-Id") UUID currentUserId
    ) {
        return ResponseEntity.ok(petFavoriteService.removeFavorite(petId, currentUserId));
    }

    @GetMapping("/api/users/{userId}/favorite-pets")
    public List<PetResponse> getFavoritePetsByUser(@PathVariable UUID userId) {
        return petFavoriteService.getFavoritePetsByUser(userId);
    }
}
