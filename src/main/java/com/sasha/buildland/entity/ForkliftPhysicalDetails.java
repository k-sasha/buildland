package com.sasha.buildland.entity;

import com.sasha.buildland.enums.Condition;
import com.sasha.buildland.enums.DataPlate;
import com.sasha.buildland.enums.Tire;
import com.sasha.buildland.enums.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.OneToOne;
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
@Table(name = "forklift_physical_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForkliftPhysicalDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forkliftPhysDetailSeq")
    @SequenceGenerator(name = "forkliftPhysDetailSeq", sequenceName = "forklift_phys_detail_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_plate")
    private DataPlate dataPlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "tire")
    private Tire tire;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage")
    private Stage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition")
    private Condition condition;

    @OneToOne(mappedBy = "physicalDetails")
    private Forklift forklift;

}
