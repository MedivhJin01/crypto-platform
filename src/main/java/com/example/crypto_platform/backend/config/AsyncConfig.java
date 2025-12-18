package com.example.crypto_platform.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "fetchExecutor")
    public Executor fetchExecutor() {
        return new ConcurrentTaskExecutor(Executors.newWorkStealingPool());
    }

}