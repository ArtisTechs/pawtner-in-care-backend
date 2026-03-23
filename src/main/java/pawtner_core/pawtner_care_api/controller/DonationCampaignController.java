package pawtner_core.pawtner_care_api.controller;

import java.net.URI;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
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

import pawtner_core.pawtner_care_api.dto.DonationCampaignRequest;
import pawtner_core.pawtner_care_api.dto.DonationCampaignResponse;
import pawtner_core.pawtner_care_api.dto.PageResponse;
import pawtner_core.pawtner_care_api.enums.DonationCampaignStatus;
import pawtner_core.pawtner_care_api.enums.DonationCampaignType;
import pawtner_core.pawtner_care_api.service.DonationCampaignService;

@RestController
@RequestMapping("/api/donation-campaigns")
public class DonationCampaignController {

    private final DonationCampaignService donationCampaignService;

    public DonationCampaignController(DonationCampaignService donationCampaignService) {
        this.donationCampaignService = donationCampaignService;
    }

    @GetMapping
    public PageResponse<DonationCampaignResponse> getDonationCampaigns(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) DonationCampaignStatus status,
        @RequestParam(required = false) DonationCampaignType type,
        @RequestParam(required = false) Boolean isUrgent,
        @RequestParam(required = false) BigDecimal minTotalCost,
        @RequestParam(required = false) BigDecimal maxTotalCost,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDateFrom,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDateTo,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate deadlineFrom,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate deadlineTo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam(defaultValue = "true") boolean ignorePagination
    ) {
        return donationCampaignService.getDonationCampaigns(
            search,
            title,
            status,
            type,
            isUrgent,
            minTotalCost,
            maxTotalCost,
            startDateFrom,
            startDateTo,
            deadlineFrom,
            deadlineTo,
            page,
            size,
            sortBy,
            sortDir,
            ignorePagination
        );
    }

    @GetMapping("/types")
    public List<DonationCampaignType> getDonationCampaignTypes() {
        return Arrays.asList(DonationCampaignType.values());
    }

    @GetMapping("/{id}")
    public DonationCampaignResponse getDonationCampaign(@PathVariable UUID id) {
        return donationCampaignService.getDonationCampaign(id);
    }

    @PostMapping
    public ResponseEntity<DonationCampaignResponse> createDonationCampaign(@Valid @RequestBody DonationCampaignRequest request) {
        DonationCampaignResponse response = donationCampaignService.createDonationCampaign(request);
        return ResponseEntity
            .created(URI.create("/api/donation-campaigns/" + response.id()))
            .body(response);
    }

    @PutMapping("/{id}")
    public DonationCampaignResponse updateDonationCampaign(@PathVariable UUID id, @Valid @RequestBody DonationCampaignRequest request) {
        return donationCampaignService.updateDonationCampaign(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonationCampaign(@PathVariable UUID id) {
        donationCampaignService.deleteDonationCampaign(id);
        return ResponseEntity.noContent().build();
    }
}
