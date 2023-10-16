package com.sasha.buildland.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;

@Entity
@Table(name = "forklifts")
@Getter
@Setter
@NoArgsConstructor
public class Forklift {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forkliftSeq")
    @SequenceGenerator(name = "forkliftSeq", sequenceName = "forklift_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "model")
    private String model;

    @Column(name = "capacity")
    private int capacity;

    @Column(name = "year")
    private int year;

    @Column(name = "hours")
    private Long hours;

    @Column(name = "location")
    private String location;

    @Column(name = "status")
    private String status;

    public Forklift(Long id, String manufacturer, String model, int capacity, int year, Long hours,
                    String location, String status) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.model = model;
        this.capacity = capacity;
        this.year = year;
        this.hours = hours;
        this.location = location;
        this.status = status;
    }
}
