package com.extradict.jobqueue.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final StringRedisTemplate redisTemplate;

    public static final String JOB_QUEUE     = "job_queue";
    public static final String DEAD_LETTER_Q = "dead_letter_queue";

    // Producer side — push job ID to LEFT of list
    public void pushToQueue(UUID jobId) {
        redisTemplate.opsForList().leftPush(JOB_QUEUE, jobId.toString());
        log.info("Pushed job {} to queue", jobId);
    }

    // Check queue depth — useful for metrics later
    public long getQueueDepth() {
        Long size = redisTemplate.opsForList().size(JOB_QUEUE);
        return size != null ? size : 0;
    }

    // Worker side — pop job ID from RIGHT of list (BRPOP)
    // Blocks for 5 seconds waiting for a job
    public String popFromQueue() {
        return redisTemplate.opsForList().rightPop(
                JOB_QUEUE,
                java.time.Duration.ofSeconds(5)
        );
    }
}
