package pawtner_core.pawtner_care_api.community.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.community.entity.Hashtag;

public interface HashtagRepository extends JpaRepository<Hashtag, UUID> {

    Optional<Hashtag> findByNormalizedName(String normalizedName);

    List<Hashtag> findByNormalizedNameIn(Collection<String> normalizedNames);
}

