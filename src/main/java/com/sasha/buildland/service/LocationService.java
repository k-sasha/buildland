package com.sasha.buildland.service;

import com.sasha.buildland.entity.Location;

import java.util.List;

public interface LocationService {
    Location saveLocation(Location location);
    List<Location> getAllLocations();
    void deleteLocation(Long locationId);
}
