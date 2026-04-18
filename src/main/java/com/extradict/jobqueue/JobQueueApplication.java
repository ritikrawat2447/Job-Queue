package com.extradict.jobqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
@EnableAsync
public class JobQueueApplication {
    public static void main(String[] args) {
        SecurityContextHolder.setStrategyName(
            SecurityContextHolder.MODE_INHERITABLETHREADLOCAL
        );
        SpringApplication.run(JobQueueApplication.class, args);
    }
}