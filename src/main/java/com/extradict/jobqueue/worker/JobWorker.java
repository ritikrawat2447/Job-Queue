package com.extradict.jobqueue.worker;

import com.extradict.jobqueue.entity.Job;
import com.extradict.jobqueue.queue.RedisQueueService;
import com.extradict.jobqueue.service.JobService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Starts the polling loop when the app starts.
     * Runs in a separate thread via @Async.
     */
    @PostConstruct
    @Async
    public void start() {
        log.info("🚀 Worker started — polling Redis queue...");

        while (running.get()) {
            try {
                pollAndProcess();
            } catch (Exception e) {
                log.error("Unexpected error in worker loop: {}", e.getMessage());
            }
        }
    }

    private void pollAndProcess() {
        // BRPOP — blocks for 5 seconds waiting for a job
        // Returns null if nothing arrives in 5 seconds
        String jobId = redisQueueService.popFromQueue();

        if (jobId == null) {
            log.debug("No jobs in queue, waiting...");
            return;
        }

        log.info("📥 Picked up job: {}", jobId);
        processJob(UUID.fromString(jobId));
    }

    private void processJob(UUID jobId) {
        Job job = null;

        try {
            // 1. Mark as RUNNING
            job = jobService.markAsRunning(jobId);
            log.info("🔄 Job {} status → RUNNING", jobId);

            // 2. Execute the job
            jobProcessor.process(job);

            // 3. Mark as SUCCESS
            jobService.markAsSuccess(jobId);
            log.info("✅ Job {} status → SUCCESS", jobId);

        } catch (Exception e) {
            log.error("❌ Job {} failed: {}", jobId, e.getMessage());

            if (job != null) {
                jobService.markAsFailed(jobId, e.getMessage());
                log.info("💀 Job {} status → FAILED", jobId);
            }
        }
    }
}