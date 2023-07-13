package com.java.service;

import com.java.DTO.CurriculumVitaeDTO;
import com.java.entity.AppUser;

public interface CvParsingService {
    CurriculumVitaeDTO parseAndSaveCv(byte[] cvFileBytes, AppUser appUser);
}