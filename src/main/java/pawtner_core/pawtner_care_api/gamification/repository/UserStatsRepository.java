package pawtner_core.pawtner_care_api.gamification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.gamification.entity.UserStats;

public interface UserStatsRepository extends JpaRepository<UserStats, UUID> {
}
