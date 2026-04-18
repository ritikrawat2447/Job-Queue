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

    /**
     * Starts AFTER the entire Spring context is fully loaded.
     * @EventListener(ContextRefreshedEvent) fires after Tomcat is up.
     * @Async runs it in a separate thread so HTTP still works.
     */
    @Async("workerExecutor")
    @EventListener(ContextRefreshedEvent.class)
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
        String jobId = redisQueueService.popFromQueue();

        if (jobId == null) {
            return;
        }

        log.info("📥 Picked up job: {}", jobId);
        processJob(UUID.fromString(jobId));
    }

    private void processJob(UUID jobId) {
        Job job = null;
        try {
            job = jobService.markAsRunning(jobId);
            log.info("🔄 Job {} status → RUNNING", jobId);

            jobProcessor.process(job);

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