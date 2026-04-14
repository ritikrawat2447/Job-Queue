package com.extradict.jobqueue.dto;

import com.extradict.jobqueue.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private UUID id;
    private String jobType;
    private JobStatus status;
    private int attempts;
    private String error;
    private String submittedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
