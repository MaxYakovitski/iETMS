package com.mayak.ietms.features.location.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;

import lombok.*;

import java.util.stream.Stream;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "location",
        uniqueConstraints = @UniqueConstraint(name = "uq_country_zip_place",
        columnNames = {"country_code", "zip_code", "place_name"}))
public class Location implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Column(name = "zip_code", nullable = false)
    private String zipCode;

    @Column(name = "place_name")
    private String placeName;

    @PrePersist
    @PreUpdate
    protected void normalizeLifecycle() {
        normalizeInPlace();
    }

    public void normalizeForSearch() {
        normalizeInPlace();
    }

    private void normalizeInPlace() {
        countryCode = normalizeUpper(countryCode);
        zipCode = normalizeUpper(zipCode);
        placeName = normalizeUpper(placeName);
    }

    private String normalizeUpper(String v) {
        return v == null ? null : v.trim().toUpperCase();
    }

    @Override
    public String toString() {
        return String.join(", ", Stream.of(countryCode, zipCode, placeName)
                .filter(s -> s != null && !s.isEmpty())
                .toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}