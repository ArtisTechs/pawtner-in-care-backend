package pawtner_core.pawtner_care_api.gamification.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.gamification.dto.HeroesWallEntryResponse;
import pawtner_core.pawtner_care_api.gamification.dto.UserAchievementResponse;
import pawtner_core.pawtner_care_api.gamification.dto.UserProgressViewResponse;
import pawtner_core.pawtner_care_api.gamification.dto.UserPublicHeroesWallResponse;
import pawtner_core.pawtner_care_api.gamification.service.AchievementService;
import pawtner_core.pawtner_care_api.gamification.service.HeroesWallService;

@RestController
@RequestMapping("/api/gamification")
public class HeroesWallController {

    private final HeroesWallService heroesWallService;
    private final AchievementService achievementService;

    public HeroesWallController(HeroesWallService heroesWallService, AchievementService achievementService) {
        this.heroesWallService = heroesWallService;
        this.achievementService = achievementService;
    }

    @GetMapping("/heroes-wall")
    public PageResponse<HeroesWallEntryResponse> getPublicHeroesWall(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return heroesWallService.getPublicHeroesWall(page, size);
    }

    @GetMapping("/users/{userId}/achievements")
    public List<UserAchievementResponse> getUserAchievements(@PathVariable UUID userId) {
        return achievementService.getUserAchievements(userId);
    }

    @GetMapping("/users/{userId}/heroes-wall")
    public UserPublicHeroesWallResponse getUserPublicHeroesWall(@PathVariable UUID userId) {
        return heroesWallService.getUserPublicHeroesWall(userId);
    }

    @GetMapping("/users/{userId}/progress")
    public UserProgressViewResponse getUserProgress(@PathVariable UUID userId) {
        return heroesWallService.getUserProgress(userId);
    }
}

