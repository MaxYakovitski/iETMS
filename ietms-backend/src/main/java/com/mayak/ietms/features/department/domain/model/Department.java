package com.mayak.ietms.features.department.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "department")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @PrePersist
    @PreUpdate
    private void normalize() {
        name = normalizeTrim(name);
        code = normalizeUpper(code);
    }

    private String normalizeTrim(String v) {
        return v == null ? null : v.trim();
    }

    private String normalizeUpper(String v) {
        return v == null ? null : v.trim().toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}