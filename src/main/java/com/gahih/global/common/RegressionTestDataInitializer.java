package com.gahih.global.common;

import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("testdata")
@Order(3)
@RequiredArgsConstructor
public class RegressionTestDataInitializer implements CommandLineRunner {

    private static final String TEST_PASSWORD = "test1234!";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.test-data.enabled:false}")
    private boolean enabled;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Transactional
    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        validateLocalRegressionDatabase();

        createTestUser(
                "test1",
                "테스트유저1",
                "test1@example.test"
        );

        createTestUser(
                "test2",
                "테스트유저2",
                "test2@example.test"
        );

        createTestUser(
                "test3",
                "테스트유저3",
                "test3@example.test"
        );

        createTestUser(
                "test4",
                "테스트유저4",
                "test4@example.test"
        );
    }

    private void validateLocalRegressionDatabase() {
        if (datasourceUrl == null
                || !datasourceUrl.contains("localhost")
                || !datasourceUrl.contains("gahih_validate_tmp")) {
            throw new IllegalStateException(
                    "Regression test data can only be initialized on local gahih_validate_tmp database."
            );
        }
    }

    private void createTestUser(String username, String nickname, String email) {
        if (memberRepository.existsByUsername(username)) {
            return;
        }

        if (memberRepository.existsByNickname(nickname)) {
            return;
        }

        if (memberRepository.existsByEmail(email)) {
            return;
        }

        Member member = Member.createUser(
                username,
                passwordEncoder.encode(TEST_PASSWORD),
                nickname,
                email
        );

        memberRepository.save(member);
    }
}