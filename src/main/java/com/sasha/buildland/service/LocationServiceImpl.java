package com.sasha.buildland.service;

import com.sasha.buildland.entity.Location;
import com.sasha.buildland.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl implements LocationService{

    @Autowired
    private LocationRepository locationRepository;

    @Override
    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }
}
