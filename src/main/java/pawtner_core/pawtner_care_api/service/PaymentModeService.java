package pawtner_core.pawtner_care_api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.dto.PaymentModeRequest;
import pawtner_core.pawtner_care_api.dto.PaymentModeResponse;
import pawtner_core.pawtner_care_api.entity.PaymentMode;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.repository.PaymentModeRepository;

@Service
public class PaymentModeService {

    private final PaymentModeRepository paymentModeRepository;

    public PaymentModeService(PaymentModeRepository paymentModeRepository) {
        this.paymentModeRepository = paymentModeRepository;
    }

    @Transactional(readOnly = true)
    public List<PaymentModeResponse> getPaymentModes() {
        return paymentModeRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PaymentModeResponse getPaymentMode(UUID id) {
        return toResponse(findPaymentMode(id));
    }

    @Transactional
    public PaymentModeResponse createPaymentMode(PaymentModeRequest request) {
        PaymentMode paymentMode = new PaymentMode();
        applyRequest(paymentMode, request);

        return toResponse(paymentModeRepository.save(paymentMode));
    }

    @Transactional
    public PaymentModeResponse updatePaymentMode(UUID id, PaymentModeRequest request) {
        PaymentMode paymentMode = findPaymentMode(id);
        applyRequest(paymentMode, request);

        return toResponse(paymentModeRepository.save(paymentMode));
    }

    @Transactional
    public void deletePaymentMode(UUID id) {
        PaymentMode paymentMode = findPaymentMode(id);
        paymentModeRepository.delete(paymentMode);
    }

    private PaymentMode findPaymentMode(UUID id) {
        return paymentModeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment mode with id " + id + " was not found"));
    }

    private void applyRequest(PaymentMode paymentMode, PaymentModeRequest request) {
        paymentMode.setName(request.name().trim());
        paymentMode.setPhotoQr(normalizeOptionalText(request.photoQr()));
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private PaymentModeResponse toResponse(PaymentMode paymentMode) {
        return new PaymentModeResponse(
            paymentMode.getId(),
            paymentMode.getName(),
            paymentMode.getPhotoQr(),
            paymentMode.getCreatedDate()
        );
    }
}
