package com.java.repository;

import com.java.DTO.JobListingDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobListingDTORepository extends JpaRepository<JobListingDTO, Long> {
}