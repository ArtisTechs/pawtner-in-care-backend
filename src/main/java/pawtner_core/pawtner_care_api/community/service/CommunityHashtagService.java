package pawtner_core.pawtner_care_api.community.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.community.entity.Hashtag;
import pawtner_core.pawtner_care_api.community.repository.HashtagRepository;

@Service
public class CommunityHashtagService {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("(?<!\\w)#([\\p{L}\\p{N}_]+)");

    private final HashtagRepository hashtagRepository;

    public CommunityHashtagService(HashtagRepository hashtagRepository) {
        this.hashtagRepository = hashtagRepository;
    }

    @Transactional
    public List<Hashtag> resolveHashtags(String content, Collection<String> explicitHashtags) {
        Set<String> normalizedNames = new LinkedHashSet<>();
        normalizedNames.addAll(extractHashtags(content));

        if (explicitHashtags != null) {
            explicitHashtags.stream()
                .map(this::normalizeHashtag)
                .filter(value -> !value.isBlank())
                .forEach(normalizedNames::add);
        }

        if (normalizedNames.isEmpty()) {
            return List.of();
        }

        Map<String, Hashtag> hashtagByNormalizedName = new LinkedHashMap<>();
        hashtagRepository.findByNormalizedNameIn(normalizedNames)
            .forEach(hashtag -> hashtagByNormalizedName.put(hashtag.getNormalizedName(), hashtag));

        normalizedNames.stream()
            .filter(normalizedName -> !hashtagByNormalizedName.containsKey(normalizedName))
            .forEach(normalizedName -> {
                Hashtag hashtag = new Hashtag();
                hashtag.setName(normalizedName);
                hashtag.setNormalizedName(normalizedName);
                hashtagByNormalizedName.put(normalizedName, hashtagRepository.save(hashtag));
            });

        return normalizedNames.stream()
            .map(hashtagByNormalizedName::get)
            .toList();
    }

    public String normalizeHashtag(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }

        trimmed = trimmed.trim().toLowerCase(Locale.ROOT);
        return trimmed.replaceAll("[^\\p{L}\\p{N}_]", "");
    }

    private Set<String> extractHashtags(String content) {
        if (content == null || content.isBlank()) {
            return Set.of();
        }

        Set<String> hashtags = new LinkedHashSet<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(content);

        while (matcher.find()) {
            String normalized = normalizeHashtag(matcher.group(1));
            if (!normalized.isBlank()) {
                hashtags.add(normalized);
            }
        }

        return hashtags;
    }
}

