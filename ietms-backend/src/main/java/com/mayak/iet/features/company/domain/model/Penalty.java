package com.mayak.iet.features.company.domain.model;

import com.mayak.iet.features.company.domain.enums.PenaltyType;
import com.mayak.iet.features.company.domain.enums.PenaltyUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "lane_penalty")
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_company_id")
    private Company customer;

    @Enumerated(EnumType.STRING)
    private PenaltyType type;

    @Column(precision = 7, scale = 2)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    private PenaltyUnit unit;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Penalty other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
