package com.extradict.jobqueue.entity;

import com.extradict.jobqueue.enums.JobStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "Job")
@Table(name = "jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    // ── Identity ─────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ── What to run ──────────────────────────────────
    @Column(name = "job_type", nullable = false, length = 100)
    private String jobType;         // e.g. "send_email", "resize_image"

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;         // JSON string of input data

    // ── State machine ────────────────────────────────
    @Enumerated(EnumType.STRING)    // stores "PENDING" not 0,1,2
    @Column(nullable = false, length = 20)
    private JobStatus status;

    // ── Retry tracking ───────────────────────────────
    @Column(nullable = false)
    private int attempts;           // how many times worker tried

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;        // max before moving to DLQ

    @Column(columnDefinition = "TEXT")
    private String error;           // last error message if failed

    // ── Who submitted ────────────────────────────────
    @Column(name = "submitted_by", length = 255)
    private String submittedBy;     // username from JWT token

    // ── Timestamps ───────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;    // when worker picked it up

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;   // when it ended (success or fail)

}
