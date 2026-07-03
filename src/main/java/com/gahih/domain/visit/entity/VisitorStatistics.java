package com.gahih.domain.visit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "visitor_statistics",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_visitor_statistics_visit_date", columnNames = "visit_date")
        }
)
public class VisitorStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visit_date", nullable = false, unique = true)
    private LocalDate visitDate;

    @Column(name = "daily_count", nullable = false)
    private long dailyCount;

    private VisitorStatistics(LocalDate visitDate, long dailyCount) {
        this.visitDate = visitDate;
        this.dailyCount = dailyCount;
    }

    public static VisitorStatistics create(LocalDate visitDate) {
        return new VisitorStatistics(visitDate, 0L);
    }

    public void increase() {
        this.dailyCount++;
    }
}