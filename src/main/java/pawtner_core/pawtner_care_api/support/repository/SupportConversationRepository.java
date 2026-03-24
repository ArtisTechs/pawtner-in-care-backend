package pawtner_core.pawtner_care_api.support.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import pawtner_core.pawtner_care_api.support.entity.SupportConversation;
import pawtner_core.pawtner_care_api.support.enums.SupportConversationStatus;

public interface SupportConversationRepository extends JpaRepository<SupportConversation, UUID>, JpaSpecificationExecutor<SupportConversation> {

    Optional<SupportConversation> findFirstByCustomerUserIdAndStatusInOrderByCreatedAtDesc(
        UUID customerUserId,
        Collection<SupportConversationStatus> statuses
    );
}

