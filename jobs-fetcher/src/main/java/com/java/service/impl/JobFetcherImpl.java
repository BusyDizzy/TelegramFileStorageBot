package com.java.service.impl;

import com.java.DTO.JobListingDTO;
import com.java.repository.JobListingDTORepository;
import com.java.service.JobFetcher;
import com.java.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class JobFetcherImpl implements JobFetcher {

    private final JobListingDTORepository jobRepository;
    private final Map<String, Strategy> strategies;

    public JobFetcherImpl(JobListingDTORepository jobRepository, Map<String, Strategy> strategies) {
        this.jobRepository = jobRepository;
        this.strategies = strategies;
    }

    @Override
    public List<JobListingDTO> getJobListingsAndSave(String query, String location, Long appUserId) {
        List<JobListingDTO> allJobListings = new ArrayList<>();
        for (Strategy strategy : strategies.values()) {
            List<JobListingDTO> jobListings = strategy.getVacancies(query, location, appUserId);
            allJobListings.addAll(jobListings);
            // Removing old entries based on website job Id
            removeExistingEntries(jobListings);
            // save jobListings to database
            jobRepository.saveAll(jobListings);
        }
        return allJobListings;
    }

    // TODO Проверить работу
    private void removeExistingEntries(List<JobListingDTO> jobListingDTOS) {
        List<JobListingDTO> databaseEntries = jobRepository.findAll();
        int counter = 0;
        for (JobListingDTO dbJob : databaseEntries) {
            if (jobListingDTOS.contains(dbJob)) {
                log.info("Duplicate (old) entry discovered in database based on job id {}. " +
                        "Removing from current search", dbJob.getWebSiteJobId());
                jobListingDTOS.remove(dbJob);
                counter++;
            }
        }
        log.info("Total removed old job listings number: {} " +
                "New job listings would be added to database: {}", counter, jobListingDTOS.size());
    }
}
