package pawtner_core.pawtner_care_api.gamification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.gamification.entity.ActivityEvent;

public interface ActivityEventRepository extends JpaRepository<ActivityEvent, UUID> {

    List<ActivityEvent> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
