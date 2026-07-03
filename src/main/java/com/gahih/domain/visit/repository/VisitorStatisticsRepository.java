package com.gahih.domain.visit.repository;

import com.gahih.domain.visit.entity.VisitorStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface VisitorStatisticsRepository extends JpaRepository<VisitorStatistics, Long> {

    Optional<VisitorStatistics> findByVisitDate(LocalDate visitDate);

    @Query("select coalesce(sum(v.dailyCount), 0) from VisitorStatistics v")
    long sumTotalCount();
}