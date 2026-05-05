package com.mayak.ietms.features.license.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Persisted license key. The actual constraints (maxUsers, expiresAt)
 * are read from the signed JWT — never stored as plain columns
 * to prevent tampering via direct DB access.
 */
@Entity
@Table(name = "licenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Raw signed JWT license key. */
    @Column(name = "license_key",  nullable = false,  unique = true, columnDefinition = "TEXT")
    private String licenseKey;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "activated_at",  nullable = false, updatable = false)
    private Instant activatedAt;
}