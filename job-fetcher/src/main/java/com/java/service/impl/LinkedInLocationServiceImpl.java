package com.java.service.impl;

import com.java.entity.LinkedInLocation;
import com.java.repository.LinkedInLocationRepository;
import com.java.service.LinkedInLocationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LinkedInLocationServiceImpl implements LinkedInLocationService {
    private final LinkedInLocationRepository linkedInLocationRepository;

    public LinkedInLocationServiceImpl(LinkedInLocationRepository linkedInLocationRepository) {
        this.linkedInLocationRepository = linkedInLocationRepository;
    }

    public Optional<String> getGeoIdByLocationName(String locationName) {
        List<LinkedInLocation> linkedInLocations = linkedInLocationRepository.findByLocationNameContains(locationName);
        if (!linkedInLocations.isEmpty()) {
            return Optional.ofNullable(linkedInLocations.get(0).getGeoId());
        }
        return Optional.empty(); // or throw exception, or any other handling of your choice
    }
}