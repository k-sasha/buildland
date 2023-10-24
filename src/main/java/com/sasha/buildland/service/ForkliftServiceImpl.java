package com.sasha.buildland.service;

import com.sasha.buildland.entity.Forklift;
import com.sasha.buildland.repository.ForkliftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ForkliftServiceImpl implements ForkliftService{

    @Autowired
    private ForkliftRepository forkliftRepository;

    @Override
    public Forklift saveForklift(Forklift forklift) {
        return forkliftRepository.save(forklift);
    }
}
