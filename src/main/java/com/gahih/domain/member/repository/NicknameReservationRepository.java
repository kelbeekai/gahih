package com.gahih.domain.member.repository;

import com.gahih.domain.member.entity.NicknameReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NicknameReservationRepository extends JpaRepository<NicknameReservation, Long> {

    boolean existsByNicknameIgnoreCaseAndExpiresAtAfter(String nickname, LocalDateTime expiresAt);

    List<NicknameReservation> findAllByExpiresAtBefore(LocalDateTime expiresAt);

    void deleteAllByExpiresAtBefore(LocalDateTime expiresAt);
}