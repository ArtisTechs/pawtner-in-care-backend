package pawtner_core.pawtner_care_api.veterinary.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClinicOpenDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    @JsonCreator
    public static ClinicOpenDay fromValue(String value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(values())
            .filter(day -> day.name().equalsIgnoreCase(value.trim()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid open day value: " + value));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }
}
