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
@Table(name = "manufacturers")
@Getter
@Setter
@NoArgsConstructor
public class Manufacturer implements InlineKeyboardObject {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "manufacturerSeq")
    @SequenceGenerator(name = "manufacturerSeq", sequenceName = "manufacturer_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "manufacturer")
    private String manufacturerName;

    @OneToMany(mappedBy = "manufacturer")
    private List<Forklift> forklifts;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.manufacturerName;
    }
}
