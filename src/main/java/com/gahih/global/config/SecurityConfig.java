package com.gahih.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/error",
                                "/h2-console/**",
                                "/",
                                "/c/*",
                                "/members/signup",
                                "/members/login",
                                "/members/find-username",
                                "/members/find-password",
                                "/members/reset-password",
                                "/email-auth/signup/**",
                                "/account-recovery/**",
                                "/api/members/signup",
                                "/api/members/login"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/c/*/posts",
                                "/c/*/posts/*",
                                "/c/*/posts/*/attachments/download-all",
                                "/c/*/posts/attachments/*/preview",
                                "/c/*/posts/attachments/*/download"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}