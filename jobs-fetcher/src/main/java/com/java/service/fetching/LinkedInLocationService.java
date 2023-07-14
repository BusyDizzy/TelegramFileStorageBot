package com.java.service.fetching;

import java.util.Optional;

public interface LinkedInLocationService {
    Optional<String> getGeoIdByLocationName(String locationName);
}