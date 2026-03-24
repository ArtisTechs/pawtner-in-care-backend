package pawtner_core.pawtner_care_api.gamification.service;

import java.util.HashMap;
import java.util.Map;

final class RuleConfigUtils {

    private RuleConfigUtils() {
    }

    static Map<String, Object> parse(String rawConfig) {
        Map<String, Object> result = new HashMap<>();
        if (rawConfig == null || rawConfig.isBlank()) {
            return result;
        }

        String trimmed = rawConfig.trim();
        if (trimmed.startsWith("{")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("}")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.isBlank()) {
            return result;
        }

        for (String pair : trimmed.split(",")) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                continue;
            }

            String key = stripQuotes(keyValue[0].trim());
            String value = stripQuotes(keyValue[1].trim());

            if (value.matches("-?\\d+")) {
                result.put(key, Long.parseLong(value));
            } else if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                result.put(key, Boolean.parseBoolean(value));
            } else {
                result.put(key, value);
            }
        }

        return result;
    }

    private static String stripQuotes(String value) {
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }
}

