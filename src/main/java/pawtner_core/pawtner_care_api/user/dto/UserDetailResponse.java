package pawtner_core.pawtner_care_api.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import pawtner_core.pawtner_care_api.user.enums.UserRole;
import pawtner_core.pawtner_care_api.gamification.dto.UserAchievementResponse;

public record UserDetailResponse(
    UUID id,
    String firstName,
    String middleName,
    String lastName,
    String email,
    String profilePicture,
    UserRole role,
    Boolean active,
    LocalDateTime createdDate,
    LocalDateTime updatedDate,
    List<UserAchievementResponse> achievements
) {
}

