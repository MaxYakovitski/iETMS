package com.mayak.iet.features.shipment.domain.model;

import com.mayak.iet.features.shipment.domain.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "shipment_timestamp")
public class ShipmentTimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(nullable = false)
    private LocalDateTime at;

    public ShipmentTimeStamp(Shipment shipment, ShipmentStatus status, LocalDateTime at) {
        this.shipment = shipment;
        this.status = status;
        this.at = at;
    }
}
