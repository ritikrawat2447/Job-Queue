package com.extradict.jobqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAsync
public class JobQueueApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobQueueApplication.class, args);
    }
}
