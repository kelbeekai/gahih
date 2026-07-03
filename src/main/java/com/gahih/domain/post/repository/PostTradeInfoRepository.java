package com.gahih.domain.post.repository;

import com.gahih.domain.post.entity.PostTradeInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostTradeInfoRepository extends JpaRepository<PostTradeInfo, Long> {

    Optional<PostTradeInfo> findByPostId(Long postId);

    void deleteByPostId(Long postId);
}