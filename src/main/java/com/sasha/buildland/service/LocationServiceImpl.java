package com.sasha.buildland.service;

import com.sasha.buildland.entity.Location;
import com.sasha.buildland.repository.LocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LocationServiceImpl implements LocationService{

    @Autowired
    private LocationRepository locationRepository;

    @Override
    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }

    @Override
    public List<Location> getAllLocations() {
        List<Location> locations= (List<Location>) locationRepository.findAll();
        return locations;
    }

    public void deleteLocation(Long locationId) {
        Optional<Location> locationOptional = locationRepository.findById(locationId);

        if (locationOptional.isPresent()) {
            locationRepository.delete(locationOptional.get());
            log.info("Location with ID '{}' has been deleted.", locationId);
        } else {
            log.warn("Location with ID '{}' not found.", locationId);
        }
    }
}
