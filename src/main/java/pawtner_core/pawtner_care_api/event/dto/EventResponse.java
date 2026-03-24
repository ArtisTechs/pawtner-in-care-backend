package pawtner_core.pawtner_care_api.event.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

public record EventResponse(
    UUID id,
    String title,
    String description,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate,
    @JsonFormat(pattern = "HH:mm")
    LocalTime time,
    String link,
    String photo,
    LocalDateTime createdDate,
    LocalDateTime updatedDate
) {
}

