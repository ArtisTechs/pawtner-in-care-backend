package pawtner_core.pawtner_care_api.controller;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
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
import pawtner_core.pawtner_care_api.dto.EmergencySosRequest;
import pawtner_core.pawtner_care_api.dto.EmergencySosResponse;
import pawtner_core.pawtner_care_api.dto.PageResponse;
import pawtner_core.pawtner_care_api.enums.EmergencySosStatus;
import pawtner_core.pawtner_care_api.enums.EmergencySosType;
import pawtner_core.pawtner_care_api.service.EmergencySosService;

@RestController
@RequestMapping("/api/emergency-sos")
public class EmergencySosController {

    private final EmergencySosService emergencySosService;

    public EmergencySosController(EmergencySosService emergencySosService) {
        this.emergencySosService = emergencySosService;
    }

    @GetMapping
    public PageResponse<EmergencySosResponse> getEmergencySosList(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UUID personFilledId,
        @RequestParam(required = false) EmergencySosType type,
        @RequestParam(required = false) EmergencySosStatus status,
        @RequestParam(required = false) String addressLocation,
        @RequestParam(required = false) String personFilledName,
        @RequestParam(required = false) String personFilledEmail,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(defaultValue = "true") boolean ignorePagination
    ) {
        return emergencySosService.getEmergencySosList(
            search,
            personFilledId,
            type,
            status,
            addressLocation,
            personFilledName,
            personFilledEmail,
            page,
            size,
            sortBy,
            sortDir,
            ignorePagination
        );
    }

    @GetMapping("/types")
    public List<EmergencySosType> getEmergencySosTypes() {
        return Arrays.asList(EmergencySosType.values());
    }

    @GetMapping("/statuses")
    public List<EmergencySosStatus> getEmergencySosStatuses() {
        return Arrays.asList(EmergencySosStatus.values());
    }

    @GetMapping("/{id}")
    public EmergencySosResponse getEmergencySos(@PathVariable UUID id) {
        return emergencySosService.getEmergencySos(id);
    }

    @PostMapping
    public ResponseEntity<EmergencySosResponse> createEmergencySos(@Valid @RequestBody EmergencySosRequest request) {
        EmergencySosResponse response = emergencySosService.createEmergencySos(request);
        return ResponseEntity
            .created(URI.create("/api/emergency-sos/" + response.id()))
            .body(response);
    }

    @PutMapping("/{id}")
    public EmergencySosResponse updateEmergencySos(@PathVariable UUID id, @Valid @RequestBody EmergencySosRequest request) {
        return emergencySosService.updateEmergencySos(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmergencySos(@PathVariable UUID id) {
        emergencySosService.deleteEmergencySos(id);
        return ResponseEntity.noContent().build();
    }
}
