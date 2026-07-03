package com.gahih.domain.post.repository;

import com.gahih.domain.post.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {

    List<PostAttachment> findAllByPostIdOrderByIdAsc(Long postId);

    Optional<PostAttachment> findByIdAndPostId(Long id, Long postId);
}
