package com.sasha.buildland.entity;

import com.sasha.buildland.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "forklifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Forklift {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forkliftSeq")
    @SequenceGenerator(name = "forkliftSeq", sequenceName = "forklift_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "inventory_number")
    private String number;

    @ManyToOne
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;

    @Column(name = "forklift_model")
    private String model;

    @Column(name = "serial_number")
    private String serial;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "forklift_status")
    private Status status;

    @OneToOne
    @JoinColumn(name = "technical_details_id")
    private ForkliftTechnicalDetails technicalDetails;

    @OneToOne
    @JoinColumn(name = "physical_details_id")
    private ForkliftPhysicalDetails physicalDetails;

    @Column(name = "sale_price")
    private int price;

    public String formatForkliftInfo() {
        StringBuilder sb = new StringBuilder();
        if (this.getNumber() != null) sb.append("Inventory Number: ").append(this.getNumber()).append("\n");
        if (this.getManufacturer() != null && this.getManufacturer().getName() != null)
            sb.append("Manufacturer: ").append(this.getManufacturer().getName()).append("\n");
        if (this.getModel() != null) sb.append("Model: ").append(this.getModel()).append("\n");
        if (this.getSerial() != null) sb.append("Serial: ").append(this.getSerial()).append("\n");
        if (this.getTechnicalDetails() != null && this.getTechnicalDetails().getLoadCapacity() > 0)
            sb.append("Capacity: ").append(this.getTechnicalDetails().getLoadCapacity()).append(" lb").append("\n");
        if (this.getLocation() != null && this.getLocation().getName() != null)
            sb.append("Location: ").append(this.getLocation().getName()).append("\n");
        if (this.getStatus() != null) sb.append("Status: ").append(this.getStatus().getDisplayName()).append("\n");
        if (this.getPrice() > 0) sb.append("Price: $").append(this.getPrice());

        return sb.toString();
    }
}
