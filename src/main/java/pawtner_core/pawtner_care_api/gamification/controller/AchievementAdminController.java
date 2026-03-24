package pawtner_core.pawtner_care_api.gamification.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.dto.PageResponse;
import pawtner_core.pawtner_care_api.gamification.dto.AchievementResponse;
import pawtner_core.pawtner_care_api.gamification.dto.AchievementStatusUpdateRequest;
import pawtner_core.pawtner_care_api.gamification.dto.AchievementUpsertRequest;
import pawtner_core.pawtner_care_api.gamification.dto.ManualAchievementAssignmentRequest;
import pawtner_core.pawtner_care_api.gamification.dto.UserAchievementResponse;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementAssignmentType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementCategory;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementRarity;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementRuleType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementTriggerType;
import pawtner_core.pawtner_care_api.gamification.enums.AchievementVisibility;
import pawtner_core.pawtner_care_api.gamification.service.AchievementMapperService;
import pawtner_core.pawtner_care_api.gamification.service.AchievementSeedService;
import pawtner_core.pawtner_care_api.gamification.service.AchievementService;

@RestController
@RequestMapping("/api/gamification/admin/achievements")
public class AchievementAdminController {

    private final AchievementService achievementService;
    private final AchievementSeedService achievementSeedService;
    private final AchievementMapperService achievementMapperService;

    public AchievementAdminController(
        AchievementService achievementService,
        AchievementSeedService achievementSeedService,
        AchievementMapperService achievementMapperService
    ) {
        this.achievementService = achievementService;
        this.achievementSeedService = achievementSeedService;
        this.achievementMapperService = achievementMapperService;
    }

    @GetMapping
    public PageResponse<AchievementResponse> getAchievements(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) AchievementCategory category,
        @RequestParam(required = false) AchievementRarity rarity,
        @RequestParam(required = false) Boolean isActive,
        @RequestParam(required = false) Boolean isRepeatable,
        @RequestParam(required = false) AchievementVisibility visibility,
        @RequestParam(required = false) AchievementAssignmentType assignmentType,
        @RequestParam(required = false) AchievementTriggerType triggerType,
        @RequestParam(required = false) AchievementRuleType ruleType,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startAtFrom,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startAtTo,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endAtFrom,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endAtTo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(defaultValue = "true") boolean ignorePagination
    ) {
        return achievementService.getAchievements(
            search,
            code,
            title,
            category,
            rarity,
            isActive,
            isRepeatable,
            visibility,
            assignmentType,
            triggerType,
            ruleType,
            startAtFrom,
            startAtTo,
            endAtFrom,
            endAtTo,
            page,
            size,
            sortBy,
            sortDir,
            ignorePagination
        );
    }

    @GetMapping("/{id}")
    public AchievementResponse getAchievement(@PathVariable UUID id) {
        return achievementService.getAchievement(id);
    }

    @PostMapping
    public ResponseEntity<AchievementResponse> createAchievement(@Valid @RequestBody AchievementUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(achievementService.createAchievement(request));
    }

    @PutMapping("/{id}")
    public AchievementResponse updateAchievement(@PathVariable UUID id, @Valid @RequestBody AchievementUpsertRequest request) {
        return achievementService.updateAchievement(id, request);
    }

    @PutMapping("/{id}/status")
    public AchievementResponse updateAchievementStatus(
        @PathVariable UUID id,
        @Valid @RequestBody AchievementStatusUpdateRequest request
    ) {
        return achievementService.updateAchievementStatus(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAchievement(@PathVariable UUID id) {
        achievementService.deleteAchievement(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    public ResponseEntity<UserAchievementResponse> manuallyAssignAchievement(
        @Valid @RequestBody ManualAchievementAssignmentRequest request
    ) {
        return ResponseEntity.ok(achievementService.manuallyAssignAchievement(request));
    }

    @PostMapping("/seed-defaults")
    public ResponseEntity<List<AchievementResponse>> seedDefaultAchievements() {
        List<AchievementResponse> response = achievementSeedService.seedDefaultAchievements().stream()
            .map(achievementMapperService::toAchievementResponse)
            .toList();
        return ResponseEntity.ok(response);
    }
}
