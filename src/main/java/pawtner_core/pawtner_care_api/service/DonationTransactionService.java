package pawtner_core.pawtner_care_api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.dto.DonationTransactionRequest;
import pawtner_core.pawtner_care_api.dto.DonationTransactionResponse;
import pawtner_core.pawtner_care_api.entity.DonationCampaign;
import pawtner_core.pawtner_care_api.entity.DonationTransaction;
import pawtner_core.pawtner_care_api.entity.PaymentMode;
import pawtner_core.pawtner_care_api.entity.User;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.repository.DonationCampaignRepository;
import pawtner_core.pawtner_care_api.repository.DonationTransactionRepository;
import pawtner_core.pawtner_care_api.repository.PaymentModeRepository;
import pawtner_core.pawtner_care_api.repository.UserRepository;

@Service
public class DonationTransactionService {

    private final DonationTransactionRepository donationTransactionRepository;
    private final PaymentModeRepository paymentModeRepository;
    private final DonationCampaignRepository donationCampaignRepository;
    private final UserRepository userRepository;

    public DonationTransactionService(
        DonationTransactionRepository donationTransactionRepository,
        PaymentModeRepository paymentModeRepository,
        DonationCampaignRepository donationCampaignRepository,
        UserRepository userRepository
    ) {
        this.donationTransactionRepository = donationTransactionRepository;
        this.paymentModeRepository = paymentModeRepository;
        this.donationCampaignRepository = donationCampaignRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DonationTransactionResponse> getDonationTransactions() {
        return donationTransactionRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public DonationTransactionResponse getDonationTransaction(UUID id) {
        return toResponse(findDonationTransaction(id));
    }

    @Transactional
    public DonationTransactionResponse createDonationTransaction(DonationTransactionRequest request) {
        DonationTransaction donationTransaction = new DonationTransaction();
        applyRequest(donationTransaction, request);

        return toResponse(donationTransactionRepository.save(donationTransaction));
    }

    @Transactional
    public DonationTransactionResponse updateDonationTransaction(UUID id, DonationTransactionRequest request) {
        DonationTransaction donationTransaction = findDonationTransaction(id);
        applyRequest(donationTransaction, request);

        return toResponse(donationTransactionRepository.save(donationTransaction));
    }

    @Transactional
    public void deleteDonationTransaction(UUID id) {
        DonationTransaction donationTransaction = findDonationTransaction(id);
        donationTransactionRepository.delete(donationTransaction);
    }

    private DonationTransaction findDonationTransaction(UUID id) {
        return donationTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Donation transaction with id " + id + " was not found"));
    }

    private PaymentMode findPaymentMode(UUID id) {
        return paymentModeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment mode with id " + id + " was not found"));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " was not found"));
    }

    private DonationCampaign findDonationCampaign(UUID id) {
        return donationCampaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Donation campaign with id " + id + " was not found"));
    }

    private void applyRequest(DonationTransaction donationTransaction, DonationTransactionRequest request) {
        donationTransaction.setUser(findUser(request.userId()));
        donationTransaction.setPaymentMode(findPaymentMode(request.paymentModeId()));
        donationTransaction.setDonationCampaign(findDonationCampaign(request.donationCampaignId()));
        donationTransaction.setPhotoProof(request.photoProof().trim());
        donationTransaction.setDonatedAmount(request.donatedAmount());
        donationTransaction.setMessage(normalizeOptionalText(request.message()));
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private DonationTransactionResponse toResponse(DonationTransaction donationTransaction) {
        return new DonationTransactionResponse(
            donationTransaction.getId(),
            donationTransaction.getUser().getId(),
            buildUserFullName(donationTransaction.getUser()),
            donationTransaction.getUser().getEmail(),
            donationTransaction.getPaymentMode().getId(),
            donationTransaction.getPaymentMode().getName(),
            donationTransaction.getDonationCampaign().getId(),
            donationTransaction.getDonationCampaign().getTitle(),
            donationTransaction.getPhotoProof(),
            donationTransaction.getDonatedAmount(),
            donationTransaction.getMessage()
        );
    }

    private String buildUserFullName(User user) {
        String middleName = normalizeOptionalText(user.getMiddleName());

        if (middleName == null) {
            return user.getFirstName() + " " + user.getLastName();
        }

        return user.getFirstName() + " " + middleName + " " + user.getLastName();
    }
}
