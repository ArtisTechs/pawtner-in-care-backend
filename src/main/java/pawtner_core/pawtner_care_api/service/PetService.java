package pawtner_core.pawtner_care_api.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.dto.PetRequest;
import pawtner_core.pawtner_care_api.dto.PetResponse;
import pawtner_core.pawtner_care_api.entity.Pet;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.repository.PetRepository;

@Service
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    public List<PetResponse> getPets() {
        return petRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PetResponse getPet(UUID id) {
        return toResponse(findPet(id));
    }

    @Transactional
    public PetResponse createPet(PetRequest request) {
        validateDates(request);

        Pet pet = new Pet();
        applyRequest(pet, request);

        return toResponse(petRepository.save(pet));
    }

    @Transactional
    public PetResponse updatePet(UUID id, PetRequest request) {
        validateDates(request);

        Pet pet = findPet(id);
        applyRequest(pet, request);

        return toResponse(petRepository.save(pet));
    }

    @Transactional
    public void deletePet(UUID id) {
        Pet pet = findPet(id);
        petRepository.delete(pet);
    }

    private Pet findPet(UUID id) {
        return petRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pet with id " + id + " was not found"));
    }

    private void applyRequest(Pet pet, PetRequest request) {
        pet.setName(request.name().trim());
        pet.setGender(request.gender().trim());
        pet.setWeight(request.weight());
        pet.setHeight(request.height());
        pet.setBirthDate(request.birthDate());
        pet.setAdoptionDate(request.adoptionDate());
        pet.setRescuedDate(request.rescuedDate());
        pet.setDescription(normalizeOptionalText(request.description()));
        pet.setPhoto(normalizeOptionalText(request.photo()));
        pet.setVideos(normalizeOptionalText(request.videos()));
        pet.setIsVaccinated(request.isVaccinated());
        pet.setType(request.type().trim());
        pet.setStatus(request.status());
    }

    private void validateDates(PetRequest request) {
        LocalDate birthDate = request.birthDate();
        LocalDate adoptionDate = request.adoptionDate();
        LocalDate rescuedDate = request.rescuedDate();

        if (birthDate != null && adoptionDate != null && adoptionDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("Adoption date cannot be earlier than birth date");
        }

        if (birthDate != null && rescuedDate != null && rescuedDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("Rescued date cannot be earlier than birth date");
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private PetResponse toResponse(Pet pet) {
        return new PetResponse(
            pet.getId(),
            pet.getName(),
            pet.getGender(),
            pet.getWeight(),
            pet.getHeight(),
            pet.getBirthDate(),
            calculateAge(pet.getBirthDate()),
            pet.getAdoptionDate(),
            pet.getRescuedDate(),
            pet.getDescription(),
            pet.getPhoto(),
            pet.getVideos(),
            pet.getIsVaccinated(),
            pet.getType(),
            pet.getStatus()
        );
    }

    private Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }

        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
