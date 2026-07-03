package com.gahih.domain.member.repository;

import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUsernameOrEmail(String username, String email);

    Optional<Member> findByUsernameAndEmail(String username, String email);

    Optional<Member> findByUsernameOrEmailAndStatus(String username, String email, MemberStatus status);

    Optional<Member> findByIdAndStatus(Long id, MemberStatus status);

    List<Member> findAllByStatus(MemberStatus status);

    List<Member> findAllByStatusAndWithdrawExpireAtBefore(MemberStatus status, LocalDateTime dateTime);

    List<Member> findAllByStatusAndFinalizedAtBefore(MemberStatus status, LocalDateTime finalizedAt);
}