package com.java.service;

import com.java.entity.AppUser;

public interface AppUserService {
    String registerUser(AppUser appuser);
    String setEmail(AppUser appUser, String email);
}