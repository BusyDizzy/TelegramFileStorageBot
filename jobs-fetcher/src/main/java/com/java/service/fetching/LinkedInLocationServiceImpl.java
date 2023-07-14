package com.java.service.fetching;

import com.java.entity.LinkedInLocation;
import com.java.repository.LinkedInLocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinkedInLocationServiceImpl implements LinkedInLocationService{
    private final LinkedInLocationRepository linkedInLocationRepository;

    public LinkedInLocationServiceImpl(LinkedInLocationRepository linkedInLocationRepository) {
        this.linkedInLocationRepository = linkedInLocationRepository;
    }

    public String getGeoIdByLocationName(String locationName) {
        List<LinkedInLocation> linkedInLocations = linkedInLocationRepository.findByLocationNameContains(locationName);
        if (!linkedInLocations.isEmpty()) {
            return linkedInLocations.get(0).getGeoId();
        }
        return null; // or throw exception, or any other handling of your choice
    }
}