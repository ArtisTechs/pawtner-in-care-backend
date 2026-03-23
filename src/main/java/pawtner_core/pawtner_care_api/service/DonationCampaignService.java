package pawtner_core.pawtner_care_api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.dto.DonationCampaignRequest;
import pawtner_core.pawtner_care_api.dto.DonationCampaignResponse;
import pawtner_core.pawtner_care_api.entity.DonationCampaign;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.repository.DonationCampaignRepository;

@Service
public class DonationCampaignService {

    private final DonationCampaignRepository donationCampaignRepository;

    public DonationCampaignService(DonationCampaignRepository donationCampaignRepository) {
        this.donationCampaignRepository = donationCampaignRepository;
    }

    @Transactional(readOnly = true)
    public List<DonationCampaignResponse> getDonationCampaigns() {
        return donationCampaignRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public DonationCampaignResponse getDonationCampaign(UUID id) {
        return toResponse(findDonationCampaign(id));
    }

    @Transactional
    public DonationCampaignResponse createDonationCampaign(DonationCampaignRequest request) {
        validateDates(request);

        DonationCampaign donationCampaign = new DonationCampaign();
        applyRequest(donationCampaign, request);

        return toResponse(donationCampaignRepository.save(donationCampaign));
    }

    @Transactional
    public DonationCampaignResponse updateDonationCampaign(UUID id, DonationCampaignRequest request) {
        validateDates(request);

        DonationCampaign donationCampaign = findDonationCampaign(id);
        applyRequest(donationCampaign, request);

        return toResponse(donationCampaignRepository.save(donationCampaign));
    }

    @Transactional
    public void deleteDonationCampaign(UUID id) {
        DonationCampaign donationCampaign = findDonationCampaign(id);
        donationCampaignRepository.delete(donationCampaign);
    }

    private DonationCampaign findDonationCampaign(UUID id) {
        return donationCampaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Donation campaign with id " + id + " was not found"));
    }

    private void applyRequest(DonationCampaign donationCampaign, DonationCampaignRequest request) {
        donationCampaign.setTitle(request.title().trim());
        donationCampaign.setDescription(normalizeOptionalText(request.description()));
        donationCampaign.setTotalCost(request.totalCost());
        donationCampaign.setDeadline(request.deadline());
        donationCampaign.setStartDate(request.startDate());
        donationCampaign.setPhoto(normalizeOptionalText(request.photo()));
        donationCampaign.setIsUrgent(request.isUrgent());
        donationCampaign.setStatus(request.status());
        donationCampaign.setType(request.type());
    }

    private void validateDates(DonationCampaignRequest request) {
        if (request.startDate() != null && request.deadline() != null && request.deadline().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Deadline cannot be earlier than start date");
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private DonationCampaignResponse toResponse(DonationCampaign donationCampaign) {
        return new DonationCampaignResponse(
            donationCampaign.getId(),
            donationCampaign.getTitle(),
            donationCampaign.getDescription(),
            donationCampaign.getTotalCost(),
            donationCampaign.getDeadline(),
            donationCampaign.getStartDate(),
            donationCampaign.getUpdatedDate(),
            donationCampaign.getPhoto(),
            donationCampaign.getIsUrgent(),
            donationCampaign.getStatus(),
            donationCampaign.getType()
        );
    }
}
