package com.gahih.domain.visit.repository;

import com.gahih.domain.visit.entity.MemberDailyVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface MemberDailyVisitRepository extends JpaRepository<MemberDailyVisit, Long> {

    boolean existsByMember_IdAndVisitDate(Long memberId, LocalDate visitDate);

    long countByVisitDate(LocalDate visitDate);
}