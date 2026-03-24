package pawtner_core.pawtner_care_api.pet.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.pet.dto.PetRequest;
import pawtner_core.pawtner_care_api.pet.dto.PetResponse;
import pawtner_core.pawtner_care_api.pet.enums.PetStatus;
import pawtner_core.pawtner_care_api.pet.service.PetService;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public PageResponse<PetResponse> getPets(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String gender,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) PetStatus status,
        @RequestParam(required = false) Boolean isVaccinated,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate birthDateFrom,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate birthDateTo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam(defaultValue = "true") boolean ignorePagination
    ) {
        return petService.getPets(
            search,
            name,
            gender,
            type,
            status,
            isVaccinated,
            birthDateFrom,
            birthDateTo,
            page,
            size,
            sortBy,
            sortDir,
            ignorePagination
        );
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

