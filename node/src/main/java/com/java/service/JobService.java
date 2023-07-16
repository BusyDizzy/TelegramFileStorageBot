package com.java.service;

import com.java.DTO.JobListingDTO;
import com.java.entity.AppUser;
import org.springframework.http.ResponseEntity;

public interface JobService {

    String generateCoversAndSendAsAttachment(AppUser appUser);

    ResponseEntity<JobListingDTO[]> collectJobs(AppUser appUser, String query, String location);

    String matchJobs(AppUser appUser, int matchPercentage);

    String showDownloadedJobs(AppUser appUser);

    String showMatchedJobs(AppUser appUser);
}