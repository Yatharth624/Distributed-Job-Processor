package com.jobprocessor.controller;

import com.jobprocessor.domain.JobStatus;
import com.jobprocessor.dto.JobResponse;
import com.jobprocessor.dto.SubmitJobRequest;
import com.jobprocessor.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> submitJob(@RequestBody SubmitJobRequest request) {
        JobResponse response = jobService.submitJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable String id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> listJobs(@RequestParam(required = false) JobStatus status) {
        return ResponseEntity.ok(jobService.listJobs(status));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<JobResponse> cancelJob(@PathVariable String id) {
        return ResponseEntity.ok(jobService.cancelJob(id));
    }
}
