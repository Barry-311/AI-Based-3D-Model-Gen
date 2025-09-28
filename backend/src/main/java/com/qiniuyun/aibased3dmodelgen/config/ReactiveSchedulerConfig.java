package com.qiniuyun.aibased3dmodelgen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ReactiveSchedulerConfig {

    @Bean(name = "tripoBlockingExecutor", destroyMethod = "shutdown")
    public ExecutorService tripoBlockingExecutor() {
        int cpu = Runtime.getRuntime().availableProcessors();
        int poolSize = Math.max(4, cpu * 2); // 可根据负载调优
        int queueCapacity = 1024;            // 有界队列，避免无限堆积

        ThreadFactory tf = new ThreadFactory() {
            private final AtomicInteger idx = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "tripo-block-" + idx.getAndIncrement());
                t.setDaemon(true); // 后台线程
                return t;
            }
        };

        return new ThreadPoolExecutor(
                poolSize,
                poolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                tf,
                new ThreadPoolExecutor.CallerRunsPolicy() // 简单背压：队列满时在调用线程执行
        );
    }

    @Bean(name = "tripoBlockingScheduler")
    public Scheduler tripoBlockingScheduler(@Qualifier("tripoBlockingExecutor") ExecutorService executor) {
        return Schedulers.fromExecutorService(executor);
    }
}