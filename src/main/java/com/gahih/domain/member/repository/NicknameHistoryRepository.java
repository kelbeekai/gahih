package com.gahih.domain.member.repository;

import com.gahih.domain.member.entity.NicknameHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NicknameHistoryRepository extends JpaRepository<NicknameHistory, Long> {

    boolean existsByPreviousNicknameIgnoreCaseAndChangedAtAfter(String previousNickname, LocalDateTime changedAt);

    List<NicknameHistory> findAllByMemberIdOrderByChangedAtDesc(Long memberId);

    List<NicknameHistory> findAllByMemberId(Long memberId);
}