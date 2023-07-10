package com.java.repository;

import com.java.entity.CurriculumVitae;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CurriculumVitaeRepository extends JpaRepository<CurriculumVitae, Long> {
    @EntityGraph(attributePaths = {"jobExperience"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT cv FROM CurriculumVitae cv WHERE cv.appUser.id = :userId")
    CurriculumVitae findByIdWithJobExperiences(@Param("userId") Long userId);
}