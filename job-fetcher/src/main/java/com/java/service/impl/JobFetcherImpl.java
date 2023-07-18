package com.java.service.impl;

import com.java.DTO.JobListingDTO;
import com.java.repository.JobListingDTORepository;
import com.java.service.JobFetcher;

import com.java.service.UrlShortenerService;
import com.java.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class JobFetcherImpl implements JobFetcher {

    private final JobListingDTORepository jobRepository;
    private final Map<String, Strategy> strategies;

    private final UrlShortenerService urlShortener;

    public JobFetcherImpl(JobListingDTORepository jobRepository, Map<String, Strategy> strategies, UrlShortenerService urlShortenerService) {
        this.jobRepository = jobRepository;
        this.strategies = strategies;
        this.urlShortener = urlShortenerService;
    }

    @Override
    public List<JobListingDTO> getJobListingsAndSave(String query, String location, Long appUserId) {
        List<JobListingDTO> allJobListings = new ArrayList<>();
        for (Strategy strategy : strategies.values()) {
            Set<JobListingDTO> jobListings = strategy.getVacancies(query, location, appUserId);
            // Removing old entries based on website job Id
            removeExistingEntries(jobListings, appUserId);
            // Возвращаем только новые
            allJobListings.addAll(jobListings);
            // save jobListings to database
            jobRepository.saveAll(jobListings);
        }
        return allJobListings;
    }

    // TODO Проверить работу
    private void removeExistingEntries(Set<JobListingDTO> jobListingDTOS, Long appUserId) {
        List<JobListingDTO> databaseEntries = jobRepository.findByUserId(appUserId);
        int counter = 0;
        for (JobListingDTO dbJob : databaseEntries) {
            if (jobListingDTOS.contains(dbJob)) {
                log.info("Duplicate (old) entry discovered in database based on job id {}. " +
                        "Removing from current search", dbJob.getWebSiteJobId());
                jobListingDTOS.remove(dbJob);
                counter++;
            }
        }

        for (JobListingDTO jobToSave : jobListingDTOS){
            // Setting up a short Url to job object that would be saved to db
            String url = jobToSave.getUrl();
            jobToSave.setUrl(urlShortener.shortenURL(url));
        }
        log.info("Total removed old job listings number: {} " +
                "New job listings would be added to database: {}", counter, jobListingDTOS.size());
    }
}
