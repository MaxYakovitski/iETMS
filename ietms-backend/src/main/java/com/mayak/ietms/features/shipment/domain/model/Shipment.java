package com.mayak.ietms.features.shipment.domain.model;

import com.mayak.ietms.features.company.domain.model.Company;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.shipment.domain.enums.ShipmentCancelReason;
import com.mayak.ietms.features.shipment.domain.enums.ShipmentStatus;
import com.mayak.ietms.shared.exception.business.DeliveryTimeLineException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment {

    @Version
    private Long version;

    @Id
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Request request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(nullable = false)
    private LocalDate plannedLoadDate;

    @Column(nullable = false)
    private LocalDate plannedDropDate;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("at ASC")
    private final List<ShipmentTimeStamp> timestamps = new ArrayList<>();

    @Setter
    @Enumerated(EnumType.STRING)
    @Column
    private ShipmentCancelReason cancelReason;

    @Setter
    @Column(columnDefinition = "text")
    private String comments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_company_id")
    private Company carrier;

    @Setter
    @Column(length = 250)
    private String licensePlate;

    @Setter
    @Column(length = 250)
    private String transportOrder;

    @JoinColumn(name = "dispatcher_id")
    private Long dispatcherId;

    public Shipment(Request request) {
        this.request = request;
        this.status = ShipmentStatus.PLANNED;
        addTimestamp(ShipmentStatus.PLANNED, Instant.now());
        this.plannedLoadDate = request.getStartDate().toLocalDate();
        this.plannedDropDate = request.getEndDate().toLocalDate();
    }

    private void addTimestamp(ShipmentStatus status, Instant at) {
        if (status == null) throw new IllegalArgumentException("Status must not be null!");
        if (at == null) throw new IllegalArgumentException("Timestamp time must not be null!");
        this.timestamps.add(new ShipmentTimeStamp(this, status, at));
    }

    public void markLoaded(Instant at) {
        if (!status.canTransitionTo(ShipmentStatus.LOADED)) {
            throw new IllegalStateException("Cannot mark as LOADED from " + status);
        }
        this.status = ShipmentStatus.LOADED;
        addTimestamp(ShipmentStatus.LOADED, at);
    }

    public void markDropped(Instant at) {
        if (!status.canTransitionTo(ShipmentStatus.DROPPED)) {
            throw new IllegalStateException("Cannot mark as DROPPED from " + status);
        }
        this.status = ShipmentStatus.DROPPED;
        addTimestamp(ShipmentStatus.DROPPED, at);
    }

    public void cancel(ShipmentCancelReason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("Cancel reason must not be null!");
        }

        if (status.isFinal()) {
            throw new IllegalStateException(
                    "Cannot cancel shipment in status " + status
            );
        }

        if (!status.canTransitionTo(ShipmentStatus.CANCELED)) {
            throw new IllegalStateException(
                    "Cannot cancel shipment from " + status
            );
        }

        this.status = ShipmentStatus.CANCELED;
        this.cancelReason = reason;
        addTimestamp(ShipmentStatus.CANCELED, Instant.now());
    }

    public void assignDispatcher(Long dispatcherId) {
        if (dispatcherId == null) throw new IllegalArgumentException("Dispatcher id must not be null!");
        this.dispatcherId = dispatcherId;
    }

    public boolean isDispatchedBy(Long userId) {
        return dispatcherId != null && dispatcherId.equals(userId);
    }

    public void assignCarrier(Company carrier) {
        if (carrier == null) {
            throw new IllegalArgumentException("Carrier must not be null!");
        }

        if (status.isFinal()) {
            throw new IllegalStateException("Cannot change carrier for final shipment status: " + status);
        }

        this.carrier = carrier;
    }

    public void unassignCarrier() {
        if (status.isFinal()) {
            throw new IllegalStateException("Cannot remove carrier for final shipment status: " + status);
        }
        this.carrier = null;
    }

    public void validateStatusChange(ShipmentStatus target, Instant at) {
        if (target == ShipmentStatus.LOADED) {

            LocalDate eventDate = at.atZone(ZoneOffset.UTC).toLocalDate();
            if (eventDate.isBefore(plannedLoadDate)) {
                throw new DeliveryTimeLineException("Actual loading time cannot be before planned loading time!");
            }
        }

        if (target == ShipmentStatus.DROPPED) {
            Instant loadedAt = getLastTimestamp(ShipmentStatus.LOADED);

            if (loadedAt == null) {
                throw new DeliveryTimeLineException("Cannot drop shipment that was not loaded!");
            }

            if (at.isBefore(loadedAt)) {
                throw new DeliveryTimeLineException("Dropping time cannot be before loading time!");
            }
        }
    }

    private Instant getLastTimestamp(ShipmentStatus status) {
        return timestamps.stream()
                .filter(t -> t.getStatus() == status)
                .map(ShipmentTimeStamp::getAt)
                .max(Instant::compareTo)
                .orElse(null);
    }

    public Instant plannedAt() {return getLastTimestamp(ShipmentStatus.PLANNED);}
    public Instant loadedAt() {return getLastTimestamp(ShipmentStatus.LOADED);}
    public Instant droppedAt() {return getLastTimestamp(ShipmentStatus.DROPPED);}
    public Instant canceledAt() {return getLastTimestamp(ShipmentStatus.CANCELED);}

    public boolean isLoadedBeforeOrOn(LocalDate date) {
        Instant t = loadedAt();
        if (t == null) return false;
        LocalDate eventDate = t.atZone(ZoneOffset.UTC).toLocalDate();
        return !eventDate.isAfter(date);
    }

    public boolean isDroppedBeforeOrOn(LocalDate date) {
        Instant t = droppedAt();
        if (t == null) return false;
        LocalDate eventDate = t.atZone(ZoneOffset.UTC).toLocalDate();
        return !eventDate.isAfter(date);
    }

    public boolean isOwnedBy(Long userId) {
        return request != null && request.getAuthorId().equals(userId);
    }
}