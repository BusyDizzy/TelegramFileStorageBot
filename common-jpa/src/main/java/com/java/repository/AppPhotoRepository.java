package com.java.repository;

import com.java.entity.AppPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppPhotoRepository extends JpaRepository<AppPhoto, Long> {
}