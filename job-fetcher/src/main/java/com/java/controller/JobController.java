package com.java.controller;

import com.java.DTO.JobListingDTO;
import com.java.DTO.JobRequestDTO;
import com.java.service.JobFetcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@Slf4j
public class JobController {

    private final JobFetcher jobFetcher;

    public JobController(JobFetcher jobFetcher) {
        this.jobFetcher = jobFetcher;
    }

    @PostMapping("/search")
    public List<JobListingDTO> searchJobs(@RequestBody JobRequestDTO jobRequestDTO) {
        return jobFetcher.getJobListingsAndSave(jobRequestDTO.getQuery(), jobRequestDTO.getLocation(),
                jobRequestDTO.getAppUserId());
    }
}