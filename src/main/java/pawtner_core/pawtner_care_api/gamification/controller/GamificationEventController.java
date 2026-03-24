package pawtner_core.pawtner_care_api.gamification.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.gamification.dto.ActivityEventRequest;
import pawtner_core.pawtner_care_api.gamification.service.ActivityEventService;

@RestController
@RequestMapping("/api/gamification/events")
public class GamificationEventController {

    private final ActivityEventService activityEventService;

    public GamificationEventController(ActivityEventService activityEventService) {
        this.activityEventService = activityEventService;
    }

    @PostMapping
    public ResponseEntity<ActivityEventService.RecordedActivityEventResult> recordEvent(
        @Valid @RequestBody ActivityEventRequest request
    ) {
        return ResponseEntity.ok(activityEventService.recordEventAndUnlocks(request));
    }
}

