package com.java.service;

import java.util.Optional;

public interface LinkedInLocationService {
    Optional<String> getGeoIdByLocationName(String locationName);
}