package com.gahih.global.common;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.category.repository.CategoryRepository;
import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostTradeInfo;
import com.gahih.domain.post.enumtype.TradeType;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.domain.post.repository.PostTradeInfoRepository;
import com.gahih.domain.reaction.enumtype.ReactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository; // 게시글 생성에 필요
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostTradeInfoRepository postTradeInfoRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void run(String... args) {
        initializeDummyMembers();
        initializeTestMembers();
        initializePosts();
        initializeComments();
    }

    private void initializeTestMembers() {
        if (!memberRepository.existsByUsername("gahihadmin")) {
            Member adminMember = Member.createAdmin(
                    "gahihadmin",
                    passwordEncoder.encode("test1234!"),
                    "가힣관리자",
                    "gahihadmin@gahih.com"
            );
            memberRepository.save(adminMember);
        }

        if (!memberRepository.existsByUsername("gahihuser")) {
            Member userMember = Member.createUser(
                    "gahihuser",
                    passwordEncoder.encode("test1234!"),
                    "가힣유저",
                    "gahihuser@gahih.com"
            );
            memberRepository.save(userMember);
        }
    }

    private void initializePosts() {
        if (postRepository.count() > 0) {
            return;
        }

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new IllegalStateException("테스트용 카테고리를 찾을 수 없습니다.");
        }

        List<Member> allMembers = memberRepository.findAll();

        List<Member> activeAdmins = allMembers.stream()
                .filter(Member::isActive)
                .filter(member -> member.getRole() == MemberRole.ADMIN)
                .toList();

        List<Member> activeUsers = allMembers.stream()
                .filter(Member::isActive)
                .toList();

        if (activeAdmins.isEmpty()) {
            throw new IllegalStateException("활성 관리자 계정을 찾을 수 없습니다.");
        }

        if (activeUsers.isEmpty()) {
            throw new IllegalStateException("활성 일반/관리자 계정을 찾을 수 없습니다.");
        }

        initPosts(categories, activeAdmins, activeUsers);
    }

    public void initPosts(List<Category> categories, List<Member> activeAdmins, List<Member> activeUsers) {
        int categoryCount = categories.size();
        int adminCount = activeAdmins.size();
        int writerCount = activeUsers.size();
        int marketPostIndex = 0;

        for (int i = 1; i <= 400; i++) {
            Category category = categories.get((i - 1) % categoryCount);

            Member writer = category.isAdminWriteOnly()
                    ? activeAdmins.get((i - 1) % adminCount)
                    : activeUsers.get((i - 1) % writerCount);

            boolean secret = category.isCode(CategoryCode.INQUIRY) && i % 7 == 0;

            Post post = Post.create(
                    writer,
                    category,
                    "[" + category.getName() + "] 테스트 게시글 " + i,
                    "테스트 내용입니다. category = " + category.getName() + ", index = " + i,
                    secret
            );

            if (i % 3 == 0) {
                post.update(
                        category,
                        "[" + category.getName() + "] 수정된 게시글 " + i,
                        "수정된 내용 " + i,
                        secret
                );
            }

            if (i % 2 == 0) {
                post.increaseViewCount();
                post.increaseViewCount();
            }

            if (category.isReactionAllowed() && i % 5 == 0) {
                post.applyReactionChange(null, ReactionType.LIKE);
            }

            Post savedPost = postRepository.save(post);

            if (category.isCode(CategoryCode.MARKET)) {
                postTradeInfoRepository.save(PostTradeInfo.create(savedPost, selectTradeType(marketPostIndex)));
                marketPostIndex++;
            }
        }
    }

    private TradeType selectTradeType(int index) {
        return switch (index % 3) {
            case 0 -> TradeType.GIVE;
            case 1 -> TradeType.SELL;
            default -> TradeType.WANTED;
        };
    }

    private void initializeComments() {
        if (commentRepository.count() > 0) {
            return;
        }

        List<Post> posts = postRepository.findAllByOrderByIdDesc();
        if (posts.isEmpty()) {
            return;
        }

        List<Member> activeMembers = memberRepository.findAll().stream()
                .filter(Member::isActive)
                .toList();

        if (activeMembers.isEmpty()) {
            return;
        }

        int targetPostCount = Math.min(10, posts.size());

        for (int i = 0; i < targetPostCount; i++) {
            Post post = posts.get(i * 40);

            if (!post.isActive()) {
                continue;
            }

            if (!post.getCategory().isCommentAllowed()) {
                continue;
            }

            initCommentsForPost(post, activeMembers);
        }
    }

    private void initCommentsForPost(Post post, List<Member> activeMembers) {
        int memberCount = activeMembers.size();
        int commentTargetCount = Math.min(100, memberCount);

        for (int i = 0; i < commentTargetCount; i++) {
            Member writer = activeMembers.get(i % memberCount);

            Comment comment = Comment.create(
                    post,
                    writer,
                    "테스트 댓글입니다. postId = " + post.getId() + ", writer = " + writer.getNickname() + ", index = " + (i + 1)
            );

            if (i % 4 == 0) {
                comment.updateContent("수정된 테스트 댓글입니다. postId = " + post.getId() + ", index = " + (i + 1));
            }

            commentRepository.save(comment);
        }
    }

    private void initializeDummyMembers() {
        if (memberRepository.count() >= 102) {
            return;
        }

        for (int i = 1; i <= 100; i++) {
            String username = "test" + i;
            String nickname = "테스트회원" + i;
            String email = "test" + i + "@gahih.com";

            if (memberRepository.existsByUsername(username)) {
                continue;
            }

            Member member;
            if (i % 10 == 0) {
                member = Member.createAdmin(
                        username,
                        passwordEncoder.encode("test1234!"),
                        nickname,
                        email
                );
            } else {
                member = Member.createUser(
                        username,
                        passwordEncoder.encode("test1234!"),
                        nickname,
                        email
                );
            }

            // 상태 분산
            if (i % 15 == 0) {
                member.suspend();
            } else if (i % 22 == 0) {
                member.markDeleted();
            }

            memberRepository.save(member);
        }
    }
}