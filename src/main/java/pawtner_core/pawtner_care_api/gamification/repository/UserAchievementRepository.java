package pawtner_core.pawtner_care_api.gamification.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.gamification.entity.UserAchievement;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {

    boolean existsByUserIdAndAchievement_IdAndIsUnlockedTrue(UUID userId, UUID achievementId);

    boolean existsByAchievement_Id(UUID achievementId);

    List<UserAchievement> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<UserAchievement> findByUserIdAndIsUnlockedTrueOrderByUnlockedAtDesc(UUID userId, Boolean isUnlocked);

    List<UserAchievement> findByIsUnlockedTrue(Boolean isUnlocked);

    Optional<UserAchievement> findFirstByUserIdAndAchievement_IdOrderByCreatedAtDesc(UUID userId, UUID achievementId);
}
