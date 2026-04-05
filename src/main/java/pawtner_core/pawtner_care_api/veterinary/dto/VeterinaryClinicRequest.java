package pawtner_core.pawtner_care_api.veterinary.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pawtner_core.pawtner_care_api.veterinary.enums.ClinicOpenDay;

public record VeterinaryClinicRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    String name,

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description,

    @NotBlank(message = "Location address is required")
    @Size(max = 255, message = "Location address must not exceed 255 characters")
    String locationAddress,

    @NotNull(message = "Longitude is required")
    @JsonProperty("long")
    BigDecimal longitude,

    @NotNull(message = "Latitude is required")
    BigDecimal latitude,

    @JsonFormat(pattern = "HH:mm")
    LocalTime openingTime,

    @JsonFormat(pattern = "HH:mm")
    LocalTime closingTime,

    List<ClinicOpenDay> openDays,

    List<
        @Size(max = 50, message = "Each contact number must not exceed 50 characters")
        String
    > contactNumbers,

    @Size(max = 100, message = "Ratings must not exceed 100 characters")
    String ratings,

    List<
        @Size(max = 500, message = "Each photo must not exceed 500 characters")
        String
    > photos,

    @Size(max = 500, message = "Logo must not exceed 500 characters")
    String logo,

    List<
        @Size(max = 500, message = "Each video must not exceed 500 characters")
        String
    > videos
) {
}

