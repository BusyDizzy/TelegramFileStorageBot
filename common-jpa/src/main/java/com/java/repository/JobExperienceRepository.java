package com.java.repository;

import com.java.entity.JobExperience;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobExperienceRepository extends JpaRepository<JobExperience, Long> {
}