package pawtner_core.pawtner_care_api.pet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import pawtner_core.pawtner_care_api.pet.enums.PetStatus;

public record PetRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name,

    @NotBlank(message = "Gender is required")
    @Size(max = 20, message = "Gender must not exceed 20 characters")
    String gender,

    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    BigDecimal weight,

    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than 0")
    BigDecimal height,

    @PastOrPresent(message = "Birth date cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthDate,

    @PastOrPresent(message = "Adoption date cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate adoptionDate,

    UUID adoptedById,

    @PastOrPresent(message = "Rescued date cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate rescuedDate,

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description,

    @Size(max = 500, message = "Photo must not exceed 500 characters")
    String photo,

    @Size(max = 500, message = "Videos must not exceed 500 characters")
    String videos,

    Boolean isVaccinated,

    @NotBlank(message = "Type is required")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    String type,

    @NotNull(message = "Status is required")
    PetStatus status
) {
}

