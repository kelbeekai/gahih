package com.gahih.global.policy;

import com.gahih.global.common.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class RecentPostBypassPolicyService {

    public void activateBypass(HttpServletRequest request, Long postId) {
        HttpSession session = request.getSession();
        Set<Long> activePostIds = getOrCreateActivePostIds(session);
        activePostIds.add(postId);
        session.setAttribute(SessionConst.ACTIVE_CREATED_POST_BYPASS, activePostIds);
    }

    public boolean isBypassActive(HttpServletRequest request, Long postId) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Set<Long> activePostIds = getOrCreateActivePostIds(session);
        return activePostIds.contains(postId);
    }

    public void deactivateBypass(HttpServletRequest request, Long postId) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        Set<Long> activePostIds = getOrCreateActivePostIds(session);
        activePostIds.remove(postId);
        session.setAttribute(SessionConst.ACTIVE_CREATED_POST_BYPASS, activePostIds);
    }

    public Set<Long> getActivePostIds(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Set.of();
        }

        return Set.copyOf(getOrCreateActivePostIds(session));
    }

    public void clearAll(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        session.setAttribute(SessionConst.ACTIVE_CREATED_POST_BYPASS, new HashSet<Long>());
    }

    @SuppressWarnings("unchecked")
    private Set<Long> getOrCreateActivePostIds(HttpSession session) {
        Object attribute = session.getAttribute(SessionConst.ACTIVE_CREATED_POST_BYPASS);

        if (attribute instanceof Set<?> set) {
            return (Set<Long>) set;
        }

        Set<Long> newSet = new HashSet<>();
        session.setAttribute(SessionConst.ACTIVE_CREATED_POST_BYPASS, newSet);
        return newSet;
    }
}