package com.gahih.domain.member.service;

import com.gahih.domain.admin.entity.AdminLog;
import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import com.gahih.domain.admin.repository.AdminLogRepository;
import com.gahih.domain.comment.repository.CommentMentionRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.entity.NicknameReservation;
import com.gahih.domain.member.repository.NicknameReservationRepository;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.domain.report.service.ReportSnapshotAnonymizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberFinalizationService {

    private static final long NICKNAME_REUSE_BLOCK_DAYS = 90;

    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    private final NicknameReservationRepository nicknameReservationRepository;

    private final ReportSnapshotAnonymizationService reportSnapshotAnonymizationService;

    private final AdminLogRepository adminLogRepository;

    private final CommentMentionRepository commentMentionRepository;

    public void finalizeMember(Member member) {
        if (member.isDeleted()) {
            return;
        }

        unpinAllPinnedPostsOfMember(member.getId());

        String originalNickname = member.getNickname();
        saveNicknameReservationForFinalizedMember(originalNickname);

        String anonymizedUsername = createAnonymizedUsername(member.getId());
        String anonymizedNickname = createAnonymizedNickname(member.getId());
        String anonymizedEmail = createAnonymizedEmail(member.getId());
        String unusablePassword = passwordEncoder.encode(
                "deleted-member-" + member.getId() + "-" + System.nanoTime()
        );

        member.finalizeDeletion(
                anonymizedUsername,
                anonymizedNickname,
                anonymizedEmail,
                unusablePassword
        );

        reportSnapshotAnonymizationService.anonymizeByFinalizedMember(member.getId());
        anonymizeCommentMentionSnapshotsForFinalizedMember(member.getId());
        anonymizeAdminLogsForFinalizedMember(member.getId(), originalNickname);

        log.info(
                "Member finalization completed. memberId={}, anonymizedUsername={}",
                member.getId(),
                member.getUsername()
        );
    }

    private void unpinAllPinnedPostsOfMember(Long memberId) {
        List<Post> pinnedPosts = postRepository.findAllByMemberIdAndPinnedTrue(memberId);

        for (Post post : pinnedPosts) {
            post.unpin();
        }
    }

    private String createAnonymizedUsername(Long memberId) {
        return "deleted_" + memberId;
    }

    private String createAnonymizedNickname(Long memberId) {
        return "탈퇴" + memberId;
    }

    private String createAnonymizedEmail(Long memberId) {
        return "deleted_" + memberId + "@deleted.local";
    }

    private void saveNicknameReservationForFinalizedMember(String nickname) {
        NicknameReservation nicknameReservation =
                NicknameReservation.reserveForMemberFinalized(nickname, NICKNAME_REUSE_BLOCK_DAYS);
        nicknameReservationRepository.save(nicknameReservation);
    }

    private void anonymizeAdminLogsForFinalizedMember(Long memberId, String originalNickname) {
        List<AdminLog> adminLogs = adminLogRepository.findAllByOrderByIdDesc();

        for (AdminLog adminLog : adminLogs) {
            if (isRelatedToFinalizedMember(adminLog, memberId, originalNickname)) {
                adminLog.anonymizeMemberNicknameData(originalNickname);
            }
        }
    }

    private boolean isRelatedToFinalizedMember(AdminLog adminLog, Long memberId, String originalNickname) {
        if (adminLog.getTargetType() == AdminLogTargetType.MEMBER
                && adminLog.getTargetId() != null
                && adminLog.getTargetId().equals(memberId)) {
            return true;
        }

        return containsOriginalNickname(adminLog.getTargetName(), originalNickname)
                || containsOriginalNickname(adminLog.getReason(), originalNickname)
                || containsOriginalNickname(adminLog.getBeforeSnapshot(), originalNickname)
                || containsOriginalNickname(adminLog.getAfterSnapshot(), originalNickname);
    }

    private boolean containsOriginalNickname(String value, String originalNickname) {
        return value != null
                && originalNickname != null
                && !originalNickname.isBlank()
                && value.contains(originalNickname);
    }

    private void anonymizeCommentMentionSnapshotsForFinalizedMember(Long memberId) {
        commentMentionRepository.anonymizeMentionSnapshotsByMentionedMemberId(memberId, "탈퇴한 회원");
    }
}