package com.extradict.jobqueue.controller;

import com.extradict.jobqueue.dto.JobRequest;
import com.extradict.jobqueue.dto.JobResponse;
import com.extradict.jobqueue.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request) {

        // submittedBy hardcoded for now
        // in Role 2 fix we'll extract from JWT token
        JobResponse response = jobService.createJob(request, "producer_one");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID id) {
        JobResponse response = jobService.getJob(id);
        return ResponseEntity.ok(response);
    }
}
