package pawtner_core.pawtner_care_api.controller;

import java.net.URI;
import java.util.Arrays;
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

import pawtner_core.pawtner_care_api.dto.DonationCampaignRequest;
import pawtner_core.pawtner_care_api.dto.DonationCampaignResponse;
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
    public List<DonationCampaignResponse> getDonationCampaigns() {
        return donationCampaignService.getDonationCampaigns();
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
