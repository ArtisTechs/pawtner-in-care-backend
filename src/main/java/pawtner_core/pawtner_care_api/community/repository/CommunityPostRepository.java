package pawtner_core.pawtner_care_api.community.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import pawtner_core.pawtner_care_api.community.entity.CommunityPost;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, UUID>, JpaSpecificationExecutor<CommunityPost> {
}
