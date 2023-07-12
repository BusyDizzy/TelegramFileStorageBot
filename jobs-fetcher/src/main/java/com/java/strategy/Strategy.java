package com.java.strategy;


import com.java.DTO.JobListingDTO;


import java.util.List;

public interface Strategy {
    List<JobListingDTO> getVacancies(String query, String location, Long appUserId);
}