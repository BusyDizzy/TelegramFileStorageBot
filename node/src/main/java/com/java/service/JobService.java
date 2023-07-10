package com.java.service;

import com.java.DTO.JobListingDTO;
import com.java.entity.AppUser;
import com.java.entity.JobListing;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface JobService {
    CompletableFuture<List<String>> generateCoverLetters(AppUser appUser, List<JobListing> jobList);

    String showJobs(AppUser appUser);

    //    boolean collectJobs(String query, String location);
    ResponseEntity<JobListingDTO[]> collectJobs(String query, String location);
}