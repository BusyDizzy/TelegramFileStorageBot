package com.java.service;

import com.java.DTO.JobListingDTO;

import java.util.List;

public interface JobFetcher {
    List<JobListingDTO> getJobListingsAndSave(String query, String location, Long appUserId);
}