package com.sasha.buildland.service;

import com.sasha.buildland.entity.Manufacturer;

import java.util.List;

public interface ManufacturerService {
    Manufacturer saveManufacturer(Manufacturer manufacturer);
    List<Manufacturer> getAllManufacturers();
    void deleteManufacturer(Long manufacturerId);
}
