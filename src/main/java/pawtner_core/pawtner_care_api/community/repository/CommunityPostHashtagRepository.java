package pawtner_core.pawtner_care_api.community.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.community.entity.CommunityPostHashtag;

public interface CommunityPostHashtagRepository extends JpaRepository<CommunityPostHashtag, UUID> {

    List<CommunityPostHashtag> findByPostId(UUID postId);

    List<CommunityPostHashtag> findByPostIdIn(Collection<UUID> postIds);

    void deleteByPostId(UUID postId);
}
