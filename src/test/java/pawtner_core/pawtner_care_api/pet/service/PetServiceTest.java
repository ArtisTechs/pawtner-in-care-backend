package pawtner_core.pawtner_care_api.pet.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.pet.entity.Pet;
import pawtner_core.pawtner_care_api.pet.repository.AdoptionRequestRepository;
import pawtner_core.pawtner_care_api.pet.repository.PetFavoriteRepository;
import pawtner_core.pawtner_care_api.pet.repository.PetRepository;
import pawtner_core.pawtner_care_api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private AdoptionRequestRepository adoptionRequestRepository;

    @Mock
    private PetFavoriteRepository petFavoriteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PetService petService;

    @Test
    void deletePetRemovesFavoritesBeforeDeletingPet() {
        UUID petId = UUID.randomUUID();
        Pet pet = new Pet();
        pet.setId(petId);

        when(petRepository.findByIdAndDeletedFalse(petId)).thenReturn(Optional.of(pet));
        when(adoptionRequestRepository.existsByPetId(petId)).thenReturn(false);

        petService.deletePet(petId);

        verify(petFavoriteRepository).deleteByPetId(petId);
        verify(petRepository).delete(pet);
    }

    @Test
    void deletePetSoftDeletesWhenStillReferenced() {
        UUID petId = UUID.randomUUID();
        Pet pet = new Pet();
        pet.setId(petId);

        when(petRepository.findByIdAndDeletedFalse(petId)).thenReturn(Optional.of(pet));
        when(adoptionRequestRepository.existsByPetId(petId)).thenReturn(true);

        petService.deletePet(petId);

        verify(petFavoriteRepository).deleteByPetId(petId);
        verify(petRepository).save(pet);
        org.junit.jupiter.api.Assertions.assertTrue(pet.isDeleted());
    }

    @Test
    void getPetDoesNotReturnSoftDeletedPets() {
        UUID petId = UUID.randomUUID();
        when(petRepository.findByIdAndDeletedFalse(petId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> petService.getPet(petId));
    }
}
