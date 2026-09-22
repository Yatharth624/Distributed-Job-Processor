package com.jobprocessor.controller;

import com.jobprocessor.dto.SystemMetricsResponse;
import com.jobprocessor.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/metrics")
@CrossOrigin(origins = "*")
public class MetricsController {

    @Autowired
    private JobService jobService;

    @GetMapping
    public ResponseEntity<SystemMetricsResponse> getMetrics() {
        return ResponseEntity.ok(jobService.getSystemMetrics());
    }
}
