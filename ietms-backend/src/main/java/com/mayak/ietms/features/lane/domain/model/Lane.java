package com.mayak.ietms.features.lane.domain.model;

import com.mayak.ietms.features.lane.domain.enums.LaneType;
import com.mayak.ietms.features.company.domain.model.Company;
import com.mayak.ietms.features.location.domain.model.Location;
import com.mayak.ietms.features.request.domain.enums.ShipmentType;
import com.mayak.ietms.features.request.domain.enums.TransportType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "company_lanes")
public class Lane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_company_id")
    private Company customer;

    private String laneName;
    private String route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_location_id")
    private Location fromLocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_location_id")
    private Location toLocation;

    @Enumerated(EnumType.STRING)
    private ShipmentType shipmentType;

    @Enumerated(EnumType.STRING)
    private TransportType transportType;

    @Column(name = "temperature")
    private String temperature;

    @Column(name = "weight")
    private Double weight;

    @Column(precision = 7, scale = 2)
    private BigDecimal price;

    @Column(precision = 7, scale = 2)
    private BigDecimal fuelSurcharge;

    private LocalDate validFrom;
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    private LaneType type;

    @ManyToOne
    @JoinColumn(name = "linked_lane_id")
    private Lane linkedLane;

    public BigDecimal getTotalPrice() {
        if (price == null || fuelSurcharge == null) return null;
        return price
                .multiply(BigDecimal.ONE.add(fuelSurcharge))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lane other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}