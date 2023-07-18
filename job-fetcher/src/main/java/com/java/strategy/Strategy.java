package com.java.strategy;


import com.java.DTO.JobListingDTO;

import java.util.Set;

public interface Strategy {
    Set<JobListingDTO> getVacancies(String query, String location, Long appUserId);
}