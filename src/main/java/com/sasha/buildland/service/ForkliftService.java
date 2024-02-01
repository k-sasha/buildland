package com.sasha.buildland.service;

import com.sasha.buildland.entity.Forklift;

import java.util.List;

public interface ForkliftService {
    Forklift saveForklift(Forklift forklift);
    List<Forklift> getAllForklifts();
    List<Forklift> findForkliftsByCapacity(int capacity);
}
