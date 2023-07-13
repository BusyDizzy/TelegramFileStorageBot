package com.java.repository;

import com.java.DTO.CurriculumVitaeDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriculumVitaeDTORepository extends JpaRepository<CurriculumVitaeDTO, Long> {
}