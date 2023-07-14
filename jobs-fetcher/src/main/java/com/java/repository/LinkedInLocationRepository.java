package com.java.repository;

import com.java.entity.LinkedInLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkedInLocationRepository extends JpaRepository<LinkedInLocation, Long> {
    @Query("SELECT l FROM LinkedInLocation l WHERE LOWER(l.locationName) LIKE LOWER(CONCAT('%', :locationName, '%'))")
    List<LinkedInLocation> findByLocationNameContains(String locationName);
}