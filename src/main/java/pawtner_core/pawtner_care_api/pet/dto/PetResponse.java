package pawtner_core.pawtner_care_api.pet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import pawtner_core.pawtner_care_api.pet.enums.PetStatus;

public record PetResponse(
    UUID id,
    String name,
    String gender,
    BigDecimal weight,
    BigDecimal height,
    LocalDate birthDate,
    BigDecimal age,
    LocalDate adoptionDate,
    PetAdopterResponse adoptedBy,
    LocalDate rescuedDate,
    String description,
    String photo,
    String videos,
    Boolean isVaccinated,
    String type,
    String race,
    PetStatus status
) {
}

