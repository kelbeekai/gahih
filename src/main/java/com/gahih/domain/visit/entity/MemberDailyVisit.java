package com.gahih.domain.visit.entity;

import com.gahih.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "member_daily_visit",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_daily_visit_member_date", columnNames = {"member_id", "visit_date"})
        },
        indexes = {
                @Index(name = "idx_member_daily_visit_visit_date", columnList = "visit_date"),
                @Index(name = "idx_member_daily_visit_member_id", columnList = "member_id")
        }
)
public class MemberDailyVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private MemberDailyVisit(Member member, LocalDate visitDate) {
        this.member = member;
        this.visitDate = visitDate;
    }

    public static MemberDailyVisit create(Member member, LocalDate visitDate) {
        return new MemberDailyVisit(member, visitDate);
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}