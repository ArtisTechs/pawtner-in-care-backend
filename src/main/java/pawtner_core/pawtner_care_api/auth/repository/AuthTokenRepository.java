package pawtner_core.pawtner_care_api.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.auth.entity.AuthToken;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

    Optional<AuthToken> findByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);
}
