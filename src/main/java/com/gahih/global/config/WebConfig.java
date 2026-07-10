package com.gahih.global.config;

import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.visit.service.VisitorStatisticsService;
import com.gahih.global.argumentresolver.LoginMemberArgumentResolver;
import com.gahih.global.interceptor.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RecentPostBypassInterceptor recentPostBypassInterceptor;
    private final MemberStatusPolicyInterceptor memberStatusPolicyInterceptor;
    private final VisitorStatisticsService visitorStatisticsService;
    private final MemberRepository memberRepository;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new VisitorCountInterceptor(visitorStatisticsService))
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error",
                        "/favicon.ico",
                        "/robots.txt",
                        "/sitemap.xml",
                        "/.well-known/**",
                        "/h2-console/**",
                        "/api/**",
                        "/admin",
                        "/admin/**",
                        "/c/*/admin",
                        "/c/*/admin/**",
                        "/c/*/posts/*/attachments/download-all",
                        "/c/*/posts/attachments/*/preview",
                        "/c/*/posts/attachments/*/download"
                );

        registry.addInterceptor(recentPostBypassInterceptor)
                .order(2)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error",
                        "/favicon.ico",
                        "/h2-console/**"
                );

        registry.addInterceptor(new LoginSessionValidityInterceptor(memberRepository))
                .order(3)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error",
                        "/favicon.ico",
                        "/h2-console/**"
                );

        registry.addInterceptor(new LoginCheckInterceptor())
                .order(4)
                .addPathPatterns(
                        "/c/*/posts/new",
                        "/c/*/posts/new/**",
                        "/c/*/posts/*/edit",
                        "/c/*/posts/*/delete",
                        "/c/*/posts/*/like",
                        "/c/*/posts/*/dislike",
                        "/c/*/posts/*/attachments/*/delete",
                        "/c/*/posts/*/attachments/*/admin-delete",
                        "/c/*/posts/*/comments",
                        "/c/*/posts/*/comments/**",
                        "/c/*/comments/*/like",
                        "/c/*/comments/*/dislike",
                        "/reports",
                        "/email-auth/change-email/**",
                        "/api/members/logout",
                        "/api/members/me",
                        "/c/*/mypage",
                        "/c/*/mypage/**",
                        "/mypage",
                        "/mypage/**",
                        "/members/logout",
                        "/members/withdrawn",
                        "/members/restore",
                        "/members/suspended"
                )

                .excludePathPatterns(
                        "/",
                        "/members/signup",
                        "/members/login",
                        "/api/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error",
                        "/h2-console/**"
                );

        registry.addInterceptor(memberStatusPolicyInterceptor)
                .order(5)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error",
                        "/favicon.ico",
                        "/h2-console/**"
                );

        registry.addInterceptor(new AdminCheckInterceptor())
                .order(6)
                .addPathPatterns(
                        "/admin",
                        "/admin/**",
                        "/c/*/admin",
                        "/c/*/admin/**"
                )
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error",
                        "/h2-console/**"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }
}