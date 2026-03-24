package pawtner_core.pawtner_care_api.community.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.community.entity.CommunityPostLike;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, UUID> {

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    Optional<CommunityPostLike> findByPostIdAndUserId(UUID postId, UUID userId);

    List<CommunityPostLike> findByPostIdInAndUserId(Collection<UUID> postIds, UUID userId);
}

