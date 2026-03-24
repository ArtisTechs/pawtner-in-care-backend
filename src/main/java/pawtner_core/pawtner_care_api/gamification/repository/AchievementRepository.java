package pawtner_core.pawtner_care_api.gamification.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import pawtner_core.pawtner_care_api.gamification.entity.Achievement;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementAssignmentType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementTriggerType;

public interface AchievementRepository extends JpaRepository<Achievement, UUID>, JpaSpecificationExecutor<Achievement> {

    Optional<Achievement> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    List<Achievement> findByIsActiveTrueAndAssignmentTypeAndTriggerType(
        AchievementAssignmentType assignmentType,
        AchievementTriggerType triggerType
    );
    List<Achievement> findByIsActiveTrueOrderByPointsDescTitleAsc();
}
