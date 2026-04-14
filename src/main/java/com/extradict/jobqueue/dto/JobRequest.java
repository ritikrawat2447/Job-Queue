package com.extradict.jobqueue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class JobRequest {

    @NotBlank(message = "jobType is required")
    private String jobType;         // e.g. "send_email", "resize_image"

    @NotNull(message = "payload is required")
    private Map<String, Object> payload;
}
