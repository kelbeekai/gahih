package com.gahih.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 메일 발송 전용 비동기 스레드풀.
     *
     * core/max/queue 값은 application.properties에서 조정한다.
     * 초기 운영/포트폴리오 기본값:
     * - core: 2
     * - max: 4
     * - queue: 100
     *
     * 요청이 몰려 queue가 가득 찰 경우 CallerRunsPolicy를 사용한다.
     * 즉 메일 작업을 버리지 않고 요청 스레드에서 직접 실행한다.
     * 장점: 메일 유실 방지
     * 단점: 폭주 상황에서는 일부 요청이 다시 느려질 수 있음
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor(
            @Value("${app.email-async.core-pool-size:2}") int corePoolSize,
            @Value("${app.email-async.max-pool-size:4}") int maxPoolSize,
            @Value("${app.email-async.queue-capacity:100}") int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("email-async-");
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}