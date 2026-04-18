package com.extradict.jobqueue.worker;

import com.extradict.jobqueue.entity.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class JobProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Executes the actual job logic based on jobType.
     * For L3 we simulate work with a sleep.
     * In L4 this becomes real logic per job type.
     */
    public void process(Job job) throws Exception {
        log.info("Processing job {} of type {}", job.getId(), job.getJobType());

        switch (job.getJobType()) {
            case "send_email" -> processEmailJob(job);
            case "resize_image" -> processImageJob(job);
            default -> processGenericJob(job);
        }

        log.info("Job {} processed successfully", job.getId());
    }

    private void processEmailJob(Job job) throws Exception {
        log.info("Sending email for job {}", job.getId());
        // Simulate work
        Thread.sleep(1000);
        log.info("Email sent for job {}", job.getId());
    }

    private void processImageJob(Job job) throws Exception {
        log.info("Resizing image for job {}", job.getId());
        Thread.sleep(2000);
        log.info("Image resized for job {}", job.getId());
    }

    private void processGenericJob(Job job) throws Exception {
        log.info("Processing generic job {}", job.getId());
        Thread.sleep(500);
        log.info("Generic job done {}", job.getId());
    }
}