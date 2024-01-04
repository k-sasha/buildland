package com.sasha.buildland.entity;

import com.sasha.buildland.enums.FuelType;
import com.sasha.buildland.enums.TransmissionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;

@Entity
@Table(name = "forklift_technical_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForkliftTechnicalDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forkliftTechDetailSeq")
    @SequenceGenerator(name = "forkliftTechDetailSeq", sequenceName = "forklift_tech_detail_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "engine_hours")
    private int engineHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type")
    private FuelType fuelType;

    @Column(name = "load_capacity")
    private int loadCapacity;

    @Column(name = "operating_weight")
    private int operatingWeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission_type")
    private TransmissionType transmissionType;

}
