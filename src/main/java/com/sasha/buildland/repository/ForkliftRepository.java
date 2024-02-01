package com.sasha.buildland.repository;

import com.sasha.buildland.entity.Forklift;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ForkliftRepository extends CrudRepository<Forklift, Integer> {
    @Query("SELECT f FROM Forklift f JOIN f.technicalDetails td WHERE td.loadCapacity = :capacity")
    List<Forklift> findForkliftsByLoadCapacity(@Param("capacity") int capacity);
}
