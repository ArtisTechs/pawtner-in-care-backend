package pawtner_core.pawtner_care_api.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.dto.PetRequest;
import pawtner_core.pawtner_care_api.dto.PetResponse;
import pawtner_core.pawtner_care_api.service.PetService;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public List<PetResponse> getPets() {
        return petService.getPets();
    }

    @GetMapping("/{id}")
    public PetResponse getPet(@PathVariable UUID id) {
        return petService.getPet(id);
    }

    @PostMapping
    public ResponseEntity<PetResponse> createPet(@Valid @RequestBody PetRequest request) {
        PetResponse response = petService.createPet(request);
        return ResponseEntity
            .created(URI.create("/api/pets/" + response.id()))
            .body(response);
    }

    @PutMapping("/{id}")
    public PetResponse updatePet(@PathVariable UUID id, @Valid @RequestBody PetRequest request) {
        return petService.updatePet(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable UUID id) {
        petService.deletePet(id);
        return ResponseEntity.noContent().build();
    }
}
