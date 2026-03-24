package pawtner_core.pawtner_care_api.gamification.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.gamification.entity.Achievement;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementAssignmentType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementCategory;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementRarity;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementRuleType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementTriggerType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementVisibility;
import pawtner_core.pawtner_care_api.gamification.repository.AchievementRepository;

@Service
public class AchievementSeedService {

    private final AchievementRepository achievementRepository;
    private final AchievementMapperService achievementMapperService;

    public AchievementSeedService(AchievementRepository achievementRepository, AchievementMapperService achievementMapperService) {
        this.achievementRepository = achievementRepository;
        this.achievementMapperService = achievementMapperService;
    }

    @Transactional
    public List<Achievement> seedDefaultAchievements() {
        return List.of(
            saveIfMissing(
                "REGISTERED_HERO",
                "Registered Hero",
                "Unlocks when a user completes registration.",
                AchievementCategory.REGISTRATION,
                50,
                AchievementRarity.COMMON,
                AchievementTriggerType.USER_REGISTERED,
                AchievementRuleType.BOOLEAN_ACTION,
                "{\"statField\":\"isRegistered\"}"
            ),
            saveIfMissing(
                "FIRST_ADOPTION",
                "First Adoption",
                "Unlocks after adopting the first pet.",
                AchievementCategory.ADOPTION,
                100,
                AchievementRarity.RARE,
                AchievementTriggerType.PET_ADOPTED,
                AchievementRuleType.FIRST_ACTION,
                "{\"statField\":\"totalAdoptedPets\"}"
            ),
            saveIfMissing(
                "FIVE_DONATIONS",
                "Five Donations",
                "Unlocks after making five donation actions.",
                AchievementCategory.DONATION,
                150,
                AchievementRarity.EPIC,
                AchievementTriggerType.DONATION_MADE,
                AchievementRuleType.COUNT_THRESHOLD,
                "{\"statField\":\"totalDonations\",\"target\":5}"
            ),
            saveIfMissing(
                "THREE_ACTIVE_MONTHS",
                "Three Active Months",
                "Unlocks after being active for three months.",
                AchievementCategory.ENGAGEMENT,
                120,
                AchievementRarity.RARE,
                AchievementTriggerType.USER_ACTIVE_MONTH,
                AchievementRuleType.STREAK,
                "{\"statField\":\"monthsActive\",\"target\":3}"
            )
        );
    }

    private Achievement saveIfMissing(
        String code,
        String title,
        String description,
        AchievementCategory category,
        int points,
        AchievementRarity rarity,
        AchievementTriggerType triggerType,
        AchievementRuleType ruleType,
        String ruleConfig
    ) {
        return achievementRepository.findByCode(code).orElseGet(() -> {
            Achievement achievement = new Achievement();
            achievement.setCode(code);
            achievement.setTitle(title);
            achievement.setDescription(description);
            achievement.setCategory(category);
            achievement.setPoints(points);
            achievement.setRarity(rarity);
            achievement.setIsActive(Boolean.TRUE);
            achievement.setIsRepeatable(Boolean.FALSE);
            achievement.setVisibility(AchievementVisibility.PUBLIC);
            achievement.setAssignmentType(AchievementAssignmentType.AUTO);
            achievement.setTriggerType(triggerType);
            achievement.setRuleType(ruleType);
            achievement.setRuleConfig(ruleConfig);
            return achievementRepository.save(achievement);
        });
    }
}

