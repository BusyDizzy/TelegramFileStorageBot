package com.java.repository;

import com.java.DTO.JobListingDTO;
import com.java.entity.enums.JobMatchState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobListingDTORepository extends JpaRepository<JobListingDTO, Long> {
    @Query("Select j from JobListingDTO j where j.appUser.id=:userId")
    List<JobListingDTO> findByUserId(@Param("userId") Long userId);

    @Query("Select j from JobListingDTO j where j.appUser.id=:userId and j.jobMatchState=:matched")
    List<JobListingDTO> findByUserIdMatched(@Param("userId") Long userId, JobMatchState matched);
}