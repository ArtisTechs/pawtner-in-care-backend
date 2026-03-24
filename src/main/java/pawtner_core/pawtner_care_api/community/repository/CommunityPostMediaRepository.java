package pawtner_core.pawtner_care_api.community.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.community.entity.CommunityPostMedia;

public interface CommunityPostMediaRepository extends JpaRepository<CommunityPostMedia, UUID> {

    List<CommunityPostMedia> findByPostIdOrderBySortOrderAscCreatedAtAsc(UUID postId);

    List<CommunityPostMedia> findByPostIdInOrderByPostIdAscSortOrderAscCreatedAtAsc(Collection<UUID> postIds);

    void deleteByPostId(UUID postId);
}

