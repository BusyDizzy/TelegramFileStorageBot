package com.java.repository;

import com.java.entity.LinkedInLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface LinkedInLocationRepository extends JpaRepository<LinkedInLocation, Long> {
    @Query("SELECT l FROM LinkedInLocation l WHERE LOWER(l.locationName) LIKE LOWER(CONCAT('%', :locationName, '%'))")
    List<LinkedInLocation> findByLocationNameContains(String locationName);
}