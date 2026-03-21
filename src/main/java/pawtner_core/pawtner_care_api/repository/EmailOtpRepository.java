package pawtner_core.pawtner_care_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.entity.EmailOtp;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, String purpose);
}
