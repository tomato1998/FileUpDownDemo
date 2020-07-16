package com.boss.oss.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author LiDaShan
 * @Version 1.0
 * @Date 2020/7/15
 * @Content:
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    @Scope("prototype")
    public  ThreadPoolExecutor getThreadPool(){
        return  new ThreadPoolExecutor(5, 5, 3,
                TimeUnit. SECONDS, new ArrayBlockingQueue<Runnable>(3),
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }
}
