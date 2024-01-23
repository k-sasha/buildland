package com.sasha.buildland.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.OneToMany;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import java.util.List;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
public class Location implements InlineKeyboardObject {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locationSeq")
    @SequenceGenerator(name = "locationSeq", sequenceName = "location_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "location")
    private String locationName;

    @OneToMany(mappedBy = "location")
    private List<Forklift> forklifts;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.locationName;
    }
}
