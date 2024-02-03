package com.sasha.buildland.service;

import com.sasha.buildland.entity.Forklift;
import com.sasha.buildland.repository.ForkliftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForkliftServiceImpl implements ForkliftService{

    @Autowired
    private ForkliftRepository forkliftRepository;

    @Override
    public Forklift saveForklift(Forklift forklift) {
        return forkliftRepository.save(forklift);
    }

    @Override
    public List<Forklift> getAllForklifts() {
        return (List<Forklift>) forkliftRepository.findAll();
    }

    @Override
    public List<Forklift> findForkliftsByCapacity(int capacity) {
        return forkliftRepository.findForkliftsByLoadCapacity(capacity);
    }

    @Override
    public List<Forklift> findForkliftsByPrice(int price) {
        return forkliftRepository.findForkliftsByPrice(price);
    }
}
