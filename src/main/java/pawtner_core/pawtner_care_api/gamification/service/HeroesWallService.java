package pawtner_core.pawtner_care_api.gamification.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.dto.PageResponse;
import pawtner_core.pawtner_care_api.gamification.dto.GamificationUserProfileResponse;
import pawtner_core.pawtner_care_api.gamification.dto.HeroesWallEntryResponse;
import pawtner_core.pawtner_care_api.gamification.dto.UserAchievementResponse;
import pawtner_core.pawtner_care_api.gamification.dto.UserProgressItemResponse;
import pawtner_core.pawtner_care_api.gamification.dto.UserProgressViewResponse;
import pawtner_core.pawtner_care_api.gamification.dto.UserPublicHeroesWallResponse;
import pawtner_core.pawtner_care_api.gamification.entity.Achievement;
import pawtner_core.pawtner_care_api.gamification.entity.UserAchievement;
import pawtner_core.pawtner_care_api.gamification.entity.UserStats;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementVisibility;
import pawtner_core.pawtner_care_api.gamification.repository.AchievementRepository;
import pawtner_core.pawtner_care_api.gamification.repository.UserAchievementRepository;
import pawtner_core.pawtner_care_api.gamification.rules.RuleProgress;

@Service
public class HeroesWallService {

    private final UserAchievementRepository userAchievementRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementMapperService achievementMapperService;
    private final UserStatsService userStatsService;
    private final AchievementEvaluationService achievementEvaluationService;
    private final GamificationUserProfileService gamificationUserProfileService;

    public HeroesWallService(
        UserAchievementRepository userAchievementRepository,
        AchievementRepository achievementRepository,
        AchievementMapperService achievementMapperService,
        UserStatsService userStatsService,
        AchievementEvaluationService achievementEvaluationService,
        GamificationUserProfileService gamificationUserProfileService
    ) {
        this.userAchievementRepository = userAchievementRepository;
        this.achievementRepository = achievementRepository;
        this.achievementMapperService = achievementMapperService;
        this.userStatsService = userStatsService;
        this.achievementEvaluationService = achievementEvaluationService;
        this.gamificationUserProfileService = gamificationUserProfileService;
    }

    @Transactional(readOnly = true)
    public PageResponse<HeroesWallEntryResponse> getPublicHeroesWall(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        List<UserAchievement> publicUnlocked = userAchievementRepository.findByIsUnlockedTrue(Boolean.TRUE).stream()
            .filter(userAchievement -> userAchievement.getAchievement().getVisibility() == AchievementVisibility.PUBLIC)
            .toList();

        Map<UUID, List<UserAchievement>> grouped = publicUnlocked.stream()
            .collect(Collectors.groupingBy(UserAchievement::getUserId));

        List<Map.Entry<UUID, List<UserAchievement>>> orderedUsers = grouped.entrySet().stream()
            .sorted((left, right) -> compareGroups(right.getValue(), left.getValue()))
            .toList();

        List<HeroesWallEntryResponse> entries = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<UUID, List<UserAchievement>> entry : orderedUsers) {
            List<UserAchievement> achievements = entry.getValue();
            GamificationUserProfileResponse profile = gamificationUserProfileService.getProfile(entry.getKey());

            entries.add(new HeroesWallEntryResponse(
                entry.getKey(),
                profile.displayName(),
                profile.email(),
                achievements.stream().mapToLong(userAchievement -> userAchievement.getAchievement().getPoints()).sum(),
                (long) achievements.size(),
                latestUnlockedAt(achievements),
                rank++
            ));
        }

        return toPage(entries, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public UserPublicHeroesWallResponse getUserPublicHeroesWall(UUID userId) {
        GamificationUserProfileResponse profile = gamificationUserProfileService.getProfile(userId);
        List<UserAchievementResponse> achievements = userAchievementRepository
            .findByUserIdAndIsUnlockedTrueOrderByUnlockedAtDesc(userId, Boolean.TRUE).stream()
            .filter(userAchievement -> userAchievement.getAchievement().getVisibility() == AchievementVisibility.PUBLIC)
            .map(achievementMapperService::toUserAchievementResponse)
            .toList();

        long totalPoints = achievements.stream().mapToLong(response -> response.achievement().points()).sum();
        return new UserPublicHeroesWallResponse(userId, profile, totalPoints, (long) achievements.size(), achievements);
    }

    @Transactional(readOnly = true)
    public UserProgressViewResponse getUserProgress(UUID userId) {
        UserStats userStats = userStatsService.getOrCreate(userId);
        GamificationUserProfileResponse profile = gamificationUserProfileService.getProfile(userId);

        Map<UUID, UserAchievement> latestByAchievement = userAchievementRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .collect(Collectors.toMap(
                userAchievement -> userAchievement.getAchievement().getId(),
                userAchievement -> userAchievement,
                (current, ignored) -> current
            ));

        List<UserProgressItemResponse> achievements = achievementRepository.findByIsActiveTrueOrderByPointsDescTitleAsc().stream()
            .map(achievement -> toProgressItem(achievement, latestByAchievement.get(achievement.getId()), userStats))
            .toList();

        long totalPoints = latestByAchievement.values().stream()
            .filter(userAchievement -> Boolean.TRUE.equals(userAchievement.getIsUnlocked()))
            .mapToLong(userAchievement -> userAchievement.getAchievement().getPoints())
            .sum();

        return new UserProgressViewResponse(
            userId,
            profile,
            achievementMapperService.toUserStatsResponse(userStats),
            totalPoints,
            achievements
        );
    }

    private UserProgressItemResponse toProgressItem(Achievement achievement, UserAchievement userAchievement, UserStats userStats) {
        RuleProgress progress = achievementEvaluationService.calculateProgress(achievement, userStats);
        return new UserProgressItemResponse(
            achievement.getId(),
            achievement.getCode(),
            achievement.getTitle(),
            achievement.getDescription(),
            achievement.getIconUrl(),
            achievement.getPoints(),
            userAchievement != null ? userAchievement.getProgressCurrent() : progress.current(),
            userAchievement != null ? userAchievement.getProgressTarget() : progress.target(),
            userAchievement != null ? userAchievement.getIsUnlocked() : progress.unlocked(),
            userAchievement != null ? userAchievement.getUnlockedAt() : null,
            achievement.getIsRepeatable()
        );
    }

    private int compareGroups(List<UserAchievement> left, List<UserAchievement> right) {
        long leftPoints = left.stream().mapToLong(userAchievement -> userAchievement.getAchievement().getPoints()).sum();
        long rightPoints = right.stream().mapToLong(userAchievement -> userAchievement.getAchievement().getPoints()).sum();
        int pointsCompare = Long.compare(leftPoints, rightPoints);
        if (pointsCompare != 0) {
            return pointsCompare;
        }

        int countCompare = Integer.compare(left.size(), right.size());
        if (countCompare != 0) {
            return countCompare;
        }

        return latestUnlockedAt(left).compareTo(latestUnlockedAt(right));
    }

    private LocalDateTime latestUnlockedAt(List<UserAchievement> achievements) {
        return achievements.stream()
            .map(UserAchievement::getUnlockedAt)
            .filter(java.util.Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(LocalDateTime.MIN);
    }

    private PageResponse<HeroesWallEntryResponse> toPage(List<HeroesWallEntryResponse> entries, int page, int size) {
        int fromIndex = Math.min(page * size, entries.size());
        int toIndex = Math.min(fromIndex + size, entries.size());
        List<HeroesWallEntryResponse> content = entries.subList(fromIndex, toIndex);
        int totalPages = entries.isEmpty() ? 0 : (int) Math.ceil((double) entries.size() / size);

        return new PageResponse<>(
            content,
            page,
            size,
            entries.size(),
            totalPages,
            "totalPoints",
            "desc",
            page == 0,
            page >= Math.max(totalPages - 1, 0),
            false
        );
    }
}
