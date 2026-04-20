package com.extradict.jobqueue.service;

import com.extradict.jobqueue.dto.JobRequest;
import com.extradict.jobqueue.dto.JobResponse;
import com.extradict.jobqueue.entity.Job;
import com.extradict.jobqueue.enums.JobStatus;
import com.extradict.jobqueue.queue.RedisQueueService;
import com.extradict.jobqueue.repository.JobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class JobService {

    private final JobRepository jobRepository;
    private final RedisQueueService redisQueueService;
    private final ObjectMapper objectMapper;

    // ── Metrics ───────────────────────────────────────
    private final Counter jobsSubmittedCounter;
    private final Counter jobsSuccessCounter;
    private final Counter jobsFailedCounter;
    private final Timer jobExecutionTimer;

    public JobService(JobRepository jobRepository,
                      RedisQueueService redisQueueService,
                      ObjectMapper objectMapper,
                      MeterRegistry meterRegistry) {
        this.jobRepository = jobRepository;
        this.redisQueueService = redisQueueService;
        this.objectMapper = objectMapper;

        // Register metrics
        this.jobsSubmittedCounter = Counter.builder("jobs.submitted")
                .description("Total jobs submitted")
                .register(meterRegistry);

        this.jobsSuccessCounter = Counter.builder("jobs.success")
                .description("Total jobs succeeded")
                .register(meterRegistry);

        this.jobsFailedCounter = Counter.builder("jobs.failed")
                .description("Total jobs failed")
                .register(meterRegistry);

        this.jobExecutionTimer = Timer.builder("jobs.execution.time")
                .description("Job execution duration")
                .register(meterRegistry);
    }

    public JobResponse createJob(JobRequest request, String submittedBy) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(request.getPayload());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        Job job = Job.builder()
                .id(UUID.randomUUID())
                .jobType(request.getJobType())
                .payload(payloadJson)
                .status(JobStatus.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .submittedBy(submittedBy)
                .build();

        Job savedJob = jobRepository.save(job);
        log.info("Job saved to DB: {}", savedJob.getId());

        redisQueueService.pushToQueue(savedJob.getId());

        // Increment submitted counter
        jobsSubmittedCounter.increment();

        return mapToResponse(savedJob);
    }

    public Job markAsRunning(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        job.setAttempts(job.getAttempts() + 1);
        return jobRepository.save(job);
    }

    public Job markAsSuccess(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        job.setStatus(JobStatus.SUCCESS);
        job.setFinishedAt(LocalDateTime.now());
        Job saved = jobRepository.save(job);

        // Record success + execution time
        jobsSuccessCounter.increment();
        if (job.getStartedAt() != null) {
            long durationMs = java.time.Duration.between(
                    job.getStartedAt(), job.getFinishedAt()).toMillis();
            jobExecutionTimer.record(durationMs,
                    java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        return saved;
    }

    public Job markAsFailed(UUID jobId, String errorMessage) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        job.setStatus(JobStatus.FAILED);
        job.setError(errorMessage);
        job.setFinishedAt(LocalDateTime.now());
        Job saved = jobRepository.save(job);

        // Increment failed counter
        jobsFailedCounter.increment();

        return saved;
    }

    public JobResponse getJob(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        return mapToResponse(job);
    }

    public Job getJobEntity(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
    }

    private JobResponse mapToResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .attempts(job.getAttempts())
                .error(job.getError())
                .submittedBy(job.getSubmittedBy())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .build();
    }
}