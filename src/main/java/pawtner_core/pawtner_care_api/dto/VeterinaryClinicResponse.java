package pawtner_core.pawtner_care_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record VeterinaryClinicResponse(
    UUID id,
    String name,
    String description,
    String locationAddress,
    @JsonProperty("long")
    BigDecimal longitude,
    BigDecimal latitude,
    @JsonFormat(pattern = "HH:mm")
    LocalTime openingTime,
    List<String> contactNumbers,
    String ratings,
    LocalDateTime updatedDate,
    LocalDateTime createdDate,
    List<String> photos,
    String logo,
    List<String> videos
) {
}
