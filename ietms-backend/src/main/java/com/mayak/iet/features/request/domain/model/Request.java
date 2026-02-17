package com.mayak.iet.features.request.domain.model;

import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.features.bid.domain.model.Bid;
import com.mayak.iet.features.company.domain.model.Company;
import com.mayak.iet.features.request.domain.enums.RequestStatus;
import com.mayak.iet.features.request.domain.enums.ShipmentType;
import com.mayak.iet.features.request.domain.enums.TransportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "requests")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "request_type")
@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public abstract class Request {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    // ---------------- LOCATIONS ----------------
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "from_location_ids_order", columnDefinition = "jsonb")
    private List<Long> fromLocationIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "to_location_ids_order", columnDefinition = "jsonb")
    private List<Long> toLocationIds;

    // ---------------- OTHER FIELDS ----------------
    @Column
    private String customerReference;

    @Column
    private String tid;

    @ManyToOne()
    @JoinColumn(name = "customer_company_id")
    private Company customer;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private ShipmentType shipmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private TransportType transportType;

    @Column(nullable = false)
    @ToString.Include
    private boolean dangerous;

    @Column
    @ToString.Include
    private String temperature;

    @Column
    @ToString.Include
    private Double weight;

    @Column
    @ToString.Include
    private Double loadingMeter;

    @Column(length = 1000)
    @ToString.Include
    private String comments;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status =  RequestStatus.NEW;

    @Column(name = "refuse_reason", length = 64)
    protected String refuseReason;

    @Column(nullable = false)
    @ToString.Include
    private boolean archived;

    @JoinColumn(name = "author_id", nullable = false)
    private Long authorId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime issueDate;

    @Column(precision = 7, scale = 2)
    private BigDecimal clientPrice;

    @Column(precision = 7, scale = 2)
    private BigDecimal bidPrice;

    @Column(precision = 7, scale = 2)
    private BigDecimal profitMargin;

    @ElementCollection
    @CollectionTable(name = "request_competitors", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "user_id")
    private Set <Long> competitorsId =  new HashSet<>();

    @JoinColumn(name = "dispatcher_id")
    private Long dispatcherId;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Bid> bids = new HashSet<>();


    // ---------- AUTHOR LOGIC ----------
    public boolean isAuthoredBy(Long userId) { return authorId != null && authorId.equals(userId); }

    // ---------- COMPETITOR LOGIC ----------
    public void addCompetitor(Long userId) {
        if (userId != null) competitorsId.add(userId);
    }
    public void removeCompetitor(Long userId) {
        if (userId != null) competitorsId.remove(userId);
    }

    @Transient
    public void setReason(RefuseReason reason) {
        if (reason == null) {
            this.refuseReason = null;
            return;
        }
        this.refuseReason = reason.getCode();
    }

    @Transient
    public RequestTypeDto getRequestTypeDto() {
        if (this instanceof ContractRequest) return RequestTypeDto.CONTRACT;
        if (this instanceof SpotRequest) return RequestTypeDto.SPOT;
        throw new IllegalStateException("Unknown request type: " + getClass());
    }

}