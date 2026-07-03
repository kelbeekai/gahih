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
@Profile("prod")
@Order(2)
@RequiredArgsConstructor
public class ProdAdminInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.nickname}")
    private String adminNickname;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Transactional
    @Override
    public void run(String... args) {
        if (memberRepository.existsByUsername(adminUsername)) {
            return;
        }

        Member admin = Member.createAdmin(
                adminUsername,
                passwordEncoder.encode(adminPassword),
                adminNickname,
                adminEmail
        );

        memberRepository.save(admin);
    }
}