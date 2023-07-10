package com.java.service.impl;

import com.java.DTO.JobListingDTO;
import com.java.service.OpenAIService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobMatcherImpl {

    private final OpenAIService openAIService;

    public JobMatcherImpl(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    public List<JobListingDTO> matchJobs(List<JobListingDTO> jobListingDTOS) {
        return null;
    }
}