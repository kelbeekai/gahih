package com.gahih.domain.member.repository;

import com.gahih.domain.member.entity.EmailAuthRequest;
import com.gahih.domain.member.enumtype.EmailAuthPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailAuthRequestRepository extends JpaRepository<EmailAuthRequest, Long> {

    Optional<EmailAuthRequest> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, EmailAuthPurpose purpose);

    Optional<EmailAuthRequest> findTopByEmailAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc(
            String email,
            EmailAuthPurpose purpose
    );

    Optional<EmailAuthRequest> findTopByEmailAndPurposeAndTargetMemberIdOrderByCreatedAtDesc(
            String email,
            EmailAuthPurpose purpose,
            Long targetMemberId
    );

    Optional<EmailAuthRequest> findTopByEmailAndPurposeAndTargetUsernameOrderByCreatedAtDesc(
            String email,
            EmailAuthPurpose purpose,
            String targetUsername
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from EmailAuthRequest e
        where e.expiresAt < :now
        """)
    int deleteAllExpired(@Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from EmailAuthRequest e
        where e.verifiedAt is not null
          and e.verifiedAt < :verifiedRetentionCutoff
        """)
    int deleteAllVerifiedBefore(@Param("verifiedRetentionCutoff") LocalDateTime verifiedRetentionCutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from EmailAuthRequest e
        where e.createdAt < :createdRetentionCutoff
        """)
    int deleteAllCreatedBefore(@Param("createdRetentionCutoff") LocalDateTime createdRetentionCutoff);
}