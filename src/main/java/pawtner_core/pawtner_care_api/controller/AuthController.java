package pawtner_core.pawtner_care_api.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.dto.AuthResponse;
import pawtner_core.pawtner_care_api.dto.LoginRequest;
import pawtner_core.pawtner_care_api.dto.OtpConfirmRequest;
import pawtner_core.pawtner_care_api.dto.OtpResponse;
import pawtner_core.pawtner_care_api.dto.OtpSendRequest;
import pawtner_core.pawtner_care_api.dto.SignupRequest;
import pawtner_core.pawtner_care_api.dto.UserResponse;
import pawtner_core.pawtner_care_api.service.AuthService;
import pawtner_core.pawtner_care_api.service.OtpService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/send-otp")
    public OtpResponse sendOtp(@Valid @RequestBody OtpSendRequest request) {
        return otpService.sendOtp(request);
    }

    @PostMapping("/confirm-otp")
    public OtpResponse confirmOtp(@Valid @RequestBody OtpConfirmRequest request) {
        return otpService.confirmOtp(request);
    }
}
