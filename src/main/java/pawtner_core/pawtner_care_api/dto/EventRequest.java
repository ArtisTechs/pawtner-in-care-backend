package pawtner_core.pawtner_care_api.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    String title,

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description,

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate,

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate,

    @NotNull(message = "Time is required")
    @JsonFormat(pattern = "HH:mm")
    LocalTime time,

    @Size(max = 500, message = "Link must not exceed 500 characters")
    String link,

    @Size(max = 500, message = "Photo must not exceed 500 characters")
    String photo
) {
}
