package com.java.service;

import com.java.entity.AppUser;
import org.springframework.http.ResponseEntity;

public interface AppUserService {
    String registerUser(AppUser appuser);

    String setEmail(AppUser appUser, String email);

    ResponseEntity<String> sendUserData(AppUser appUser, String message);
}