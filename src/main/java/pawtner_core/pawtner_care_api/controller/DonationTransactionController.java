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

import pawtner_core.pawtner_care_api.dto.DonationTransactionRequest;
import pawtner_core.pawtner_care_api.dto.DonationTransactionResponse;
import pawtner_core.pawtner_care_api.service.DonationTransactionService;

@RestController
@RequestMapping("/api/donation-transactions")
public class DonationTransactionController {

    private final DonationTransactionService donationTransactionService;

    public DonationTransactionController(DonationTransactionService donationTransactionService) {
        this.donationTransactionService = donationTransactionService;
    }

    @GetMapping
    public List<DonationTransactionResponse> getDonationTransactions() {
        return donationTransactionService.getDonationTransactions();
    }

    @GetMapping("/{id}")
    public DonationTransactionResponse getDonationTransaction(@PathVariable UUID id) {
        return donationTransactionService.getDonationTransaction(id);
    }

    @PostMapping
    public ResponseEntity<DonationTransactionResponse> createDonationTransaction(@Valid @RequestBody DonationTransactionRequest request) {
        DonationTransactionResponse response = donationTransactionService.createDonationTransaction(request);
        return ResponseEntity
            .created(URI.create("/api/donation-transactions/" + response.id()))
            .body(response);
    }

    @PutMapping("/{id}")
    public DonationTransactionResponse updateDonationTransaction(@PathVariable UUID id, @Valid @RequestBody DonationTransactionRequest request) {
        return donationTransactionService.updateDonationTransaction(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonationTransaction(@PathVariable UUID id) {
        donationTransactionService.deleteDonationTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
