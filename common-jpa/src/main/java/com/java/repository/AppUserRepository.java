package com.java.repository;

import com.java.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
   Optional<AppUser> findByTelegramUserId(Long id);
   Optional<AppUser> findById(Long id);
   Optional<AppUser> findByEmail(String email);
}