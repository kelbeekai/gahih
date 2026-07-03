package com.gahih.domain.visit.dto;

public record VisitorStatisticsSummary(
        long todayCount,
        long totalCount
) {
    public static VisitorStatisticsSummary of(long todayCount, long totalCount) {
        return new VisitorStatisticsSummary(todayCount, totalCount);
    }
}