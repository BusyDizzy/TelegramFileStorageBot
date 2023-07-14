package com.java.repository;

import com.java.DTO.CurriculumVitaeDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CurriculumVitaeDTORepository extends JpaRepository<CurriculumVitaeDTO, Long> {
    @Query("Select c from CurriculumVitaeDTO c where c.appUser.id=:userId")
    Optional<CurriculumVitaeDTO> findByUserId(@Param("userId") Long userId);
}