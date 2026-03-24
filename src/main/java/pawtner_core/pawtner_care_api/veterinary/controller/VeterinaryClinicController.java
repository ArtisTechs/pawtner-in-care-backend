package pawtner_core.pawtner_care_api.veterinary.controller;

import java.net.URI;
import java.util.UUID;

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

import jakarta.validation.Valid;
import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.veterinary.dto.VeterinaryClinicRequest;
import pawtner_core.pawtner_care_api.veterinary.dto.VeterinaryClinicResponse;
import pawtner_core.pawtner_care_api.veterinary.service.VeterinaryClinicService;

@RestController
@RequestMapping("/api/veterinary-clinics")
public class VeterinaryClinicController {

    private final VeterinaryClinicService veterinaryClinicService;

    public VeterinaryClinicController(VeterinaryClinicService veterinaryClinicService) {
        this.veterinaryClinicService = veterinaryClinicService;
    }

    @GetMapping
    public PageResponse<VeterinaryClinicResponse> getVeterinaryClinics(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String locationAddress,
        @RequestParam(required = false) String ratings,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "name") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam(defaultValue = "true") boolean ignorePagination
    ) {
        return veterinaryClinicService.getVeterinaryClinics(
            search,
            name,
            locationAddress,
            ratings,
            page,
            size,
            sortBy,
            sortDir,
            ignorePagination
        );
    }

    @GetMapping("/{id}")
    public VeterinaryClinicResponse getVeterinaryClinic(@PathVariable UUID id) {
        return veterinaryClinicService.getVeterinaryClinic(id);
    }

    @PostMapping
    public ResponseEntity<VeterinaryClinicResponse> createVeterinaryClinic(@Valid @RequestBody VeterinaryClinicRequest request) {
        VeterinaryClinicResponse response = veterinaryClinicService.createVeterinaryClinic(request);
        return ResponseEntity
            .created(URI.create("/api/veterinary-clinics/" + response.id()))
            .body(response);
    }

    @PutMapping("/{id}")
    public VeterinaryClinicResponse updateVeterinaryClinic(@PathVariable UUID id, @Valid @RequestBody VeterinaryClinicRequest request) {
        return veterinaryClinicService.updateVeterinaryClinic(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVeterinaryClinic(@PathVariable UUID id) {
        veterinaryClinicService.deleteVeterinaryClinic(id);
        return ResponseEntity.noContent().build();
    }
}

