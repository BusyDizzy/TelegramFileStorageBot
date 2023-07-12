package com.java.service;

import com.java.DTO.JobListingDTO;
import com.java.entity.AppUser;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface JobService {

    String generateCoversAndSendAsAttachment(AppUser appUser);

    //    boolean collectJobs(String query, String location);
    ResponseEntity<JobListingDTO[]> collectJobs(AppUser appUser, String query, String location);

    List<JobListingDTO> matchJobs(AppUser appUser, int matchPercentage);

    String showDownloadedJobs(AppUser appUser);

    String showMatchedJobs(AppUser appUser);
}