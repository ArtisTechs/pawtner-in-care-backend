package pawtner_core.pawtner_care_api.donation.controller;

import java.net.URI;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.donation.dto.DonationTransactionRequest;
import pawtner_core.pawtner_care_api.donation.dto.DonationTransactionResponse;
import pawtner_core.pawtner_care_api.donation.service.DonationTransactionService;

@RestController
@RequestMapping("/api/donation-transactions")
public class DonationTransactionController {

    private final DonationTransactionService donationTransactionService;

    public DonationTransactionController(DonationTransactionService donationTransactionService) {
        this.donationTransactionService = donationTransactionService;
    }

    @GetMapping
    public PageResponse<DonationTransactionResponse> getDonationTransactions(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UUID userId,
        @RequestParam(required = false) UUID paymentModeId,
        @RequestParam(required = false) UUID donationCampaignId,
        @RequestParam(required = false) String userName,
        @RequestParam(required = false) String userEmail,
        @RequestParam(required = false) String paymentModeName,
        @RequestParam(required = false) String donationCampaignTitle,
        @RequestParam(required = false) BigDecimal minDonatedAmount,
        @RequestParam(required = false) BigDecimal maxDonatedAmount,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam(defaultValue = "true") boolean ignorePagination
    ) {
        return donationTransactionService.getDonationTransactions(
            search,
            userId,
            paymentModeId,
            donationCampaignId,
            userName,
            userEmail,
            paymentModeName,
            donationCampaignTitle,
            minDonatedAmount,
            maxDonatedAmount,
            page,
            size,
            sortBy,
            sortDir,
            ignorePagination
        );
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

