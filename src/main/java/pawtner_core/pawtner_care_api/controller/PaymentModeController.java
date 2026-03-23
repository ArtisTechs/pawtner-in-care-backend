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

import pawtner_core.pawtner_care_api.dto.PaymentModeRequest;
import pawtner_core.pawtner_care_api.dto.PaymentModeResponse;
import pawtner_core.pawtner_care_api.service.PaymentModeService;

@RestController
@RequestMapping("/api/payment-modes")
public class PaymentModeController {

    private final PaymentModeService paymentModeService;

    public PaymentModeController(PaymentModeService paymentModeService) {
        this.paymentModeService = paymentModeService;
    }

    @GetMapping
    public List<PaymentModeResponse> getPaymentModes() {
        return paymentModeService.getPaymentModes();
    }

    @GetMapping("/{id}")
    public PaymentModeResponse getPaymentMode(@PathVariable UUID id) {
        return paymentModeService.getPaymentMode(id);
    }

    @PostMapping
    public ResponseEntity<PaymentModeResponse> createPaymentMode(@Valid @RequestBody PaymentModeRequest request) {
        PaymentModeResponse response = paymentModeService.createPaymentMode(request);
        return ResponseEntity
            .created(URI.create("/api/payment-modes/" + response.id()))
            .body(response);
    }

    @PutMapping("/{id}")
    public PaymentModeResponse updatePaymentMode(@PathVariable UUID id, @Valid @RequestBody PaymentModeRequest request) {
        return paymentModeService.updatePaymentMode(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentMode(@PathVariable UUID id) {
        paymentModeService.deletePaymentMode(id);
        return ResponseEntity.noContent().build();
    }
}
