package pawtner_core.pawtner_care_api.community.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import pawtner_core.pawtner_care_api.community.entity.CommunityPostComment;
import pawtner_core.pawtner_care_api.community.enums.CommentStatus;

public interface CommunityPostCommentRepository extends JpaRepository<CommunityPostComment, UUID> {

    Page<CommunityPostComment> findByPostIdAndStatusAndDeletedAtIsNull(UUID postId, CommentStatus status, Pageable pageable);
}
