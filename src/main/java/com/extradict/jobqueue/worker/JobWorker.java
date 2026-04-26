package com.extradict.jobqueue.worker;

import com.extradict.jobqueue.entity.Job;
import com.extradict.jobqueue.queue.RedisQueueService;
import com.extradict.jobqueue.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobWorker {

    private final JobService jobService;
    private final RedisQueueService redisQueueService;
    private final JobProcessor jobProcessor;

    private final AtomicBoolean running = new AtomicBoolean(true);

    @Async("workerExecutor")
    @EventListener(ContextRefreshedEvent.class)
    public void start() {
        log.info("Worker started — polling Redis queue...");

        while (running.get()) {
            try {
                pollAndProcess();
            } catch (Exception e) {
                log.error("Unexpected error in worker loop: {}", e.getMessage());
            }
        }
    }

    private void pollAndProcess() {
        String jobId = redisQueueService.popFromQueue();
        if (jobId == null) return;

        log.info("Picked up job: {}", jobId);
        processWithRetry(UUID.fromString(jobId));
    }

    private void processWithRetry(UUID jobId) {
        Job job = jobService.getJobEntity(jobId);
        int maxAttempts = job.getMaxAttempts();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                // Mark as RUNNING and increment attempts
                job = jobService.markAsRunning(jobId);
                log.info("Job {} → RUNNING (attempt {}/{})", jobId, attempt, maxAttempts);

                // Execute the job
                jobProcessor.process(job);

                // Success — update status and stop retrying
                jobService.markAsSuccess(jobId);
                log.info("Job {} → SUCCESS on attempt {}", jobId, attempt);
                return;

            } catch (Exception e) {
                log.error("Job {} failed on attempt {}/{}: {}",
                        jobId, attempt, maxAttempts, e.getMessage());

                if (attempt < maxAttempts) {
                    // Exponential backoff — 2s, 4s, 8s
                    long waitMs = (long) Math.pow(2, attempt) * 1000;
                    log.info("Retrying job {} in {}ms...", jobId, waitMs);

                    try {
                        Thread.sleep(waitMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    // All retries exhausted → Dead Letter Queue
                    log.error("Job {} exhausted all {} attempts → DLQ", jobId, maxAttempts);
                    jobService.markAsFailed(jobId, e.getMessage());
                    redisQueueService.pushToDeadLetterQueue(jobId);
                }
            }
        }
    }
}