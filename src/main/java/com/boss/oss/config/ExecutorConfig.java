package com.boss.oss.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author LiDaShan
 * @Version 1.0
 * @Date 2020/7/16
 * @Content:
 */
@Configuration
@Slf4j
@EnableAsync
public class ExecutorConfig {

    @Bean
    public Executor uploadExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //核心线程数，默认为 1
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        //最大线程数 默认为Integer.MAX_VALUE
        threadPoolTaskExecutor.setMaxPoolSize(5);
        //队列最大长度
        threadPoolTaskExecutor.setQueueCapacity(50);
        //配置线程池前缀
        threadPoolTaskExecutor.setThreadNamePrefix("async-service-");
        //拒绝策略
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        //线程池初始化
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
