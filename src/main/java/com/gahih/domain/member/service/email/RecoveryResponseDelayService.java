package com.gahih.domain.member.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Component
public class RecoveryResponseDelayService {

    /**
     * 계정 존재 여부를 응답 시간으로 추측하기 어렵게 만들기 위한 최소 응답 시간.
     *
     * 현재 추천 정책:
     * - minimum-response-millis = 1500
     * - jitter-min-millis = 300
     * - jitter-max-millis = 700
     * - 최종 체감 응답 시간: 약 1.8초 ~ 2.2초
     *
     * UX가 너무 느리면:
     * - minimum-response-millis = 1200
     * - jitter-min-millis = 100
     * - jitter-max-millis = 400
     *
     * 더 강하게 방어하고 싶으면:
     * - minimum-response-millis = 1500
     * - jitter-min-millis = 500
     * - jitter-max-millis = 1000
     *
     * 궁극적 고도화 방향:
     * - 메일 발송을 비동기 처리해서 SMTP 지연을 요청 응답 시간에서 분리한다.
     */
    @Value("${app.email-auth.minimum-response-millis:1500}")
    private long minimumResponseMillis;

    @Value("${app.email-auth.response-jitter-min-millis:300}")
    private long responseJitterMinMillis;

    @Value("${app.email-auth.response-jitter-max-millis:700}")
    private long responseJitterMaxMillis;

    public <T> T executeWithMinimumDuration(Supplier<T> supplier) {
        long start = System.currentTimeMillis();
        long targetDurationMillis = calculateTargetDurationMillis();

        try {
            return supplier.get();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            long remaining = targetDurationMillis - elapsed;

            if (remaining > 0) {
                sleep(remaining);
            }
        }
    }

    public void runWithMinimumDuration(Runnable runnable) {
        executeWithMinimumDuration(() -> {
            runnable.run();
            return null;
        });
    }

    private long calculateTargetDurationMillis() {
        return minimumResponseMillis + calculateJitterMillis();
    }

    private long calculateJitterMillis() {
        if (responseJitterMaxMillis <= 0) {
            return 0;
        }

        long min = Math.max(responseJitterMinMillis, 0);
        long max = Math.max(responseJitterMaxMillis, min);

        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}