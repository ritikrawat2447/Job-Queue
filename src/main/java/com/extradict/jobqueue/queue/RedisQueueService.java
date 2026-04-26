package com.extradict.jobqueue.queue;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class RedisQueueService {

    private final StringRedisTemplate redisTemplate;

    public static final String JOB_QUEUE     = "job_queue";
    public static final String DEAD_LETTER_Q = "dead_letter_queue";

    public RedisQueueService(StringRedisTemplate redisTemplate,
                             MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;

        // Live queue depth gauge — updates automatically
        Gauge.builder("queue.depth", this, RedisQueueService::getQueueDepth)
                .description("Current number of jobs in queue")
                .register(meterRegistry);

        Gauge.builder("queue.dlq.depth", this, RedisQueueService::getDeadLetterQueueDepth)
                .description("Current number of jobs in dead letter queue")
                .register(meterRegistry);
    }

    public void pushToQueue(UUID jobId) {
        redisTemplate.opsForList().leftPush(JOB_QUEUE, jobId.toString());
        log.info("Pushed job {} to queue", jobId);
    }

    public String popFromQueue() {
        return redisTemplate.opsForList().rightPop(
                JOB_QUEUE, Duration.ofSeconds(5));
    }

    public void pushToDeadLetterQueue(UUID jobId) {
        redisTemplate.opsForList().leftPush(DEAD_LETTER_Q, jobId.toString());
        log.info("Job {} pushed to dead letter queue", jobId);
    }

    public long getQueueDepth() {
        Long size = redisTemplate.opsForList().size(JOB_QUEUE);
        return size != null ? size : 0;
    }

    public long getDeadLetterQueueDepth() {
        Long size = redisTemplate.opsForList().size(DEAD_LETTER_Q);
        return size != null ? size : 0;
    }
}