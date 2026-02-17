package com.mayak.ietms.features.company.domain.model;

import com.mayak.ietms.features.lane.domain.model.Lane;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "company",  uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Lane> lanes = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Penalty> penalties = new ArrayList<>();

    public Company(String name) {
        this.name = name;
    }

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (name != null) {
            name = name.trim().toUpperCase();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}