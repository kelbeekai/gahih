package com.gahih.domain.member.repository;

import com.gahih.domain.member.entity.PasswordResetSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetSessionRepository extends JpaRepository<PasswordResetSession, Long> {

    Optional<PasswordResetSession> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<PasswordResetSession> findAllByExpiresAtBefore(LocalDateTime dateTime);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from PasswordResetSession p
        where p.expiresAt < :now
        """)
    int deleteAllExpired(@Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from PasswordResetSession p
        where p.usedAt is not null
          and p.usedAt < :usedRetentionCutoff
        """)
    int deleteAllUsedBefore(@Param("usedRetentionCutoff") LocalDateTime usedRetentionCutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from PasswordResetSession p
        where p.createdAt < :createdRetentionCutoff
        """)
    int deleteAllCreatedBefore(@Param("createdRetentionCutoff") LocalDateTime createdRetentionCutoff);
}