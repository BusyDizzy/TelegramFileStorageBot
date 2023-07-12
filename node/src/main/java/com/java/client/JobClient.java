package com.java.client;

import com.java.DTO.JobListingDTO;
import com.java.DTO.JobRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class JobClient {
    @Value("${job.controller.url}")
    private String baseUrl;
    private final RestTemplate restTemplate;

    public JobClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public ResponseEntity<JobListingDTO[]> fetchJobs(Long appUserId, String query, String location) {
        JobRequestDTO jobRequest = new JobRequestDTO();
        jobRequest.setQuery(query);
        jobRequest.setLocation(location);
        jobRequest.setAppUserId(appUserId);

        HttpEntity<JobRequestDTO> requestEntity = new HttpEntity<>(jobRequest);
        return restTemplate
                .exchange(baseUrl, HttpMethod.POST, requestEntity, JobListingDTO[].class);
    }
}
