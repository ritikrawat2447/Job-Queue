package com.extradict.jobqueue.worker;

import com.extradict.jobqueue.entity.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class JobProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void process(Job job) throws Exception {
        log.info("Processing job {} of type {}", job.getId(), job.getJobType());

        String jobType = job.getJobType();

        if ("send_email".equals(jobType)) {
            processEmailJob(job);
        } else if ("resize_image".equals(jobType)) {
            processImageJob(job);
        } else if ("failing_job".equals(jobType)) {
            processFailingJob(job);
        } else {
            processGenericJob(job);
        }

        log.info("Job {} processed successfully", job.getId());
    }

    private void processEmailJob(Job job) throws Exception {
        log.info("Sending email for job {}", job.getId());
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

    // Always fails — used to test retry + DLQ
    private void processFailingJob(Job job) throws Exception {
        log.info("⚠️ Simulating failure for job {}", job.getId());
        throw new Exception("Simulated job failure for testing retry logic");
    }
}