package com.sasha.buildland.service;

import com.sasha.buildland.entity.ForkliftTechnicalDetails;
import com.sasha.buildland.repository.ForkliftTechnicalDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ForkliftTechnicalDetailsServiceImpl implements ForkliftTechnicalDetailsService{

    @Autowired
    private ForkliftTechnicalDetailsRepository technicalDetailsRepository;

    @Override
    public ForkliftTechnicalDetails saveForkliftTechnicalDetails(ForkliftTechnicalDetails technicalDetails) {
        return technicalDetailsRepository.save(technicalDetails);
    }
}
