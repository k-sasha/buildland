package com.sasha.buildland.service;

import com.sasha.buildland.entity.Manufacturer;
import com.sasha.buildland.repository.ManufacturerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ManufacturerServiceImpl implements ManufacturerService{

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Override
    public Manufacturer saveManufacturer(Manufacturer manufacturer) {
        return manufacturerRepository.save(manufacturer);
    }

    @Override
    public List<Manufacturer> getAllManufacturers() {
        return (List<Manufacturer>) manufacturerRepository.findAll();
    }

    public void deleteManufacturer(Long manufacturerId) {
        Optional<Manufacturer> manufacturerOptional = manufacturerRepository.findById(manufacturerId);

        if (manufacturerOptional.isPresent()) {
            manufacturerRepository.delete(manufacturerOptional.get());
            log.info("Manufacturer with ID '{}' has been deleted.", manufacturerId);
        } else {
            log.warn("Manufacturer with ID '{}' not found.", manufacturerId);
        }
    }
}
