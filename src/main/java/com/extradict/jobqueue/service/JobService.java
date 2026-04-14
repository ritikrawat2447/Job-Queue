package com.extradict.jobqueue.service;

import com.extradict.jobqueue.dto.JobRequest;
import com.extradict.jobqueue.dto.JobResponse;
import com.extradict.jobqueue.entity.Job;
import com.extradict.jobqueue.enums.JobStatus;
import com.extradict.jobqueue.queue.RedisQueueService;
import com.extradict.jobqueue.repository.JobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final RedisQueueService redisQueueService;
    private final ObjectMapper objectMapper;

    public JobResponse createJob(JobRequest request, String submittedBy) {
        // 1. Convert payload map to JSON string for storage
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(request.getPayload());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        // 2. Build job entity
        Job job = Job.builder()
                .id(UUID.randomUUID())
                .jobType(request.getJobType())
                .payload(payloadJson)
                .status(JobStatus.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .submittedBy(submittedBy)
                .build();

        // 3. Save to PostgreSQL
        Job savedJob = jobRepository.save(job);
        log.info("Job saved to DB: {}", savedJob.getId());

        // 4. Push job ID to Redis queue
        redisQueueService.pushToQueue(savedJob.getId());

        // 5. Return response
        return mapToResponse(savedJob);
    }

    public JobResponse getJob(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        return mapToResponse(job);
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
