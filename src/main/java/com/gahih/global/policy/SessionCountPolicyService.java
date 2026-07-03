package com.gahih.global.policy;

import com.gahih.global.common.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Service
public class SessionCountPolicyService {

    /**
     * [레거시 정책]
     * 같은 세션에서 같은 대상에 대해 30분 동안 1회만 카운트 증가
     */
    private static final Duration COUNT_COOLDOWN = Duration.ofMinutes(30);

    /**
     * 세션 동안 1회만 증가시키는 전용 저장소
     * 방문자수처럼 "같은 세션이면 다시 올리지 않음" 정책에 사용
     */
    private static final String UNIQUE_COUNT_HISTORY = "uniqueCountHistory";

    /**
     * [레거시 정책]
     * 같은 세션에서 같은 대상에 대해 30분 동안 1회만 카운트 증가
     */
    public boolean shouldIncrease(HttpServletRequest request, SessionCountPolicyType policyType, Long targetId) {
        HttpSession session = request.getSession();

        Map<String, Long> countHistory = getOrCreateCountHistory(session);
        removeExpiredEntries(countHistory);

        String key = createKey(policyType, targetId);
        long now = System.currentTimeMillis();

        Long lastCountTime = countHistory.get(key);
        if (lastCountTime == null || isExpired(lastCountTime, now)) {
            countHistory.put(key, now);
            session.setAttribute(SessionConst.COUNT_HISTORY, countHistory);
            return true;
        }

        return false;
    }

    /**
     * 같은 세션에서는 같은 key에 대해 단 1회만 증가
     * 방문자수처럼 세션 전체 기준 1회 정책에 사용
     */
    public boolean shouldIncreaseOncePerSession(HttpServletRequest request, SessionCountPolicyType policyType, Long targetId) {
        HttpSession session = request.getSession();

        Set<String> uniqueHistory = getOrCreateUniqueHistory(session);
        String key = createKey(policyType, targetId);

        if (uniqueHistory.contains(key)) {
            return false;
        }

        uniqueHistory.add(key);
        session.setAttribute(UNIQUE_COUNT_HISTORY, uniqueHistory);
        return true;
    }

    /**
     * [현재 정책]
     * 같은 세션에서 같은 대상에 대해 같은 날짜에는 1회만 카운트 증가
     *
     * 예)
     * - POST_VIEW + 게시글 10번 + 2026-04-22 => 하루 1회
     * - ATTACHMENT_DOWNLOAD + 첨부 3번 + 2026-04-22 => 하루 1회
     * - 00시가 지나 날짜가 바뀌면 다시 1회 증가 가능
     */
    public boolean shouldIncreaseOncePerSessionPerDay(
            HttpServletRequest request,
            SessionCountPolicyType policyType,
            Long targetId,
            String dayKey
    ) {
        HttpSession session = request.getSession();

        Set<String> uniqueHistory = getOrCreateUniqueHistory(session);
        String key = createDailyKey(policyType, targetId, dayKey);

        if (uniqueHistory.contains(key)) {
            return false;
        }

        uniqueHistory.add(key);
        session.setAttribute(UNIQUE_COUNT_HISTORY, uniqueHistory);
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> getOrCreateCountHistory(HttpSession session) {
        Object attribute = session.getAttribute(SessionConst.COUNT_HISTORY);

        if (attribute instanceof Map<?, ?> map) {
            return (Map<String, Long>) map;
        }

        Map<String, Long> newHistory = new HashMap<>();
        session.setAttribute(SessionConst.COUNT_HISTORY, newHistory);
        return newHistory;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getOrCreateUniqueHistory(HttpSession session) {
        Object attribute = session.getAttribute(UNIQUE_COUNT_HISTORY);

        if (attribute instanceof Set<?> set) {
            return (Set<String>) set;
        }

        Set<String> newHistory = new HashSet<>();
        session.setAttribute(UNIQUE_COUNT_HISTORY, newHistory);
        return newHistory;
    }

    private void removeExpiredEntries(Map<String, Long> countHistory) {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = countHistory.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (isExpired(entry.getValue(), now)) {
                iterator.remove();
            }
        }
    }

    private boolean isExpired(Long lastCountTime, long now) {
        return now - lastCountTime >= COUNT_COOLDOWN.toMillis();
    }

    private String createKey(SessionCountPolicyType policyType, Long targetId) {
        return policyType.name() + ":" + targetId;
    }

    private String createDailyKey(SessionCountPolicyType policyType, Long targetId, String dayKey) {
        return policyType.name() + ":" + targetId + ":" + dayKey;
    }
}