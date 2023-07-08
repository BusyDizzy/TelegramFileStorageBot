package com.java.repository;

import com.java.entity.JobListing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobListingRepository extends JpaRepository<JobListing,Long> {
}