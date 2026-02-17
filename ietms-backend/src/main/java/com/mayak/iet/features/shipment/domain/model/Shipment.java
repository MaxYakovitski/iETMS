package com.mayak.iet.features.shipment.domain.model;

import com.mayak.iet.features.company.domain.model.Company;
import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.features.shipment.domain.enums.ShipmentCancelReason;
import com.mayak.iet.features.shipment.domain.enums.ShipmentStatus;
import com.mayak.iet.shared.exception.business.DeliveryTimeLineException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Column(length = 1000)
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
        addTimestamp(ShipmentStatus.PLANNED, LocalDateTime.now());
        this.plannedLoadDate = request.getStartDate().toLocalDate();
        this.plannedDropDate = request.getEndDate().toLocalDate();
    }

    private void addTimestamp(ShipmentStatus status, LocalDateTime at) {
        if (status == null) throw new IllegalArgumentException("Status must not be null");
        if (at == null) throw new IllegalArgumentException("Timestamp time must not be null");
        this.timestamps.add(new ShipmentTimeStamp(this, status, at));
    }

    public void markLoaded(LocalDateTime at) {
        if (!status.canTransitionTo(ShipmentStatus.LOADED)) {
            throw new IllegalStateException("Cannot mark as LOADED from " + status);
        }
        this.status = ShipmentStatus.LOADED;
        addTimestamp(ShipmentStatus.LOADED, at);
    }

    public void markDropped(LocalDateTime at) {
        if (!status.canTransitionTo(ShipmentStatus.DROPPED)) {
            throw new IllegalStateException("Cannot mark as DROPPED from " + status);
        }
        this.status = ShipmentStatus.DROPPED;
        addTimestamp(ShipmentStatus.DROPPED, at);
    }

    public void cancel(ShipmentCancelReason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("Cancel reason must not be null");
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
        addTimestamp(ShipmentStatus.CANCELED, LocalDateTime.now());
    }

    public void assignDispatcher(Long dispatcherId) {
        if (dispatcherId == null) throw new IllegalArgumentException("Dispatcher id must not be null");
        this.dispatcherId = dispatcherId;
    }

    public boolean isDispatchedBy(Long userId) {
        return dispatcherId != null && dispatcherId.equals(userId);
    }

    public void assignCarrier(Company carrier) {
        if (carrier == null) {
            throw new IllegalArgumentException("Carrier must not be null");
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

    public void validateStatusChange(ShipmentStatus target, LocalDateTime at) {
        if (target == ShipmentStatus.LOADED) {
            if (at.toLocalDate().isBefore(plannedLoadDate)) {
                throw new DeliveryTimeLineException("Loading time cannot be before planned load date");
            }
        }


        if (target == ShipmentStatus.DROPPED) {
            LocalDateTime loadedAt = getLastTimestamp(ShipmentStatus.LOADED);

            if (loadedAt == null) {
                throw new DeliveryTimeLineException("Cannot drop shipment that was not loaded");
            }

            if (at.isBefore(loadedAt)) {
                throw new DeliveryTimeLineException("Dropping time cannot be before loaded time");
            }
        }
    }

    private LocalDateTime getLastTimestamp(ShipmentStatus status) {
        return timestamps.stream()
                .filter(t -> t.getStatus() == status)
                .map(ShipmentTimeStamp::getAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    public LocalDateTime plannedAt() {return getLastTimestamp(ShipmentStatus.PLANNED);}
    public LocalDateTime loadedAt() {return getLastTimestamp(ShipmentStatus.LOADED);}
    public LocalDateTime droppedAt() {return getLastTimestamp(ShipmentStatus.DROPPED);}
    public LocalDateTime canceledAt() {return getLastTimestamp(ShipmentStatus.CANCELED);}

    public boolean isLoaded() {return loadedAt() != null;}
    public boolean isDropped() {return droppedAt() != null;}
    public boolean isCanceled() {return status == ShipmentStatus.CANCELED;}

    public boolean isLoadedBeforeOrOn(LocalDate date) {
        LocalDateTime t = loadedAt();
        return t != null && !t.toLocalDate().isAfter(date);
    }

    public boolean isDroppedBeforeOrOn(LocalDate date) {
        LocalDateTime t = droppedAt();
        return t != null && !t.toLocalDate().isAfter(date);
    }

    public boolean isOwnedBy(Long userId) {
        return request != null && request.getAuthorId().equals(userId);
    }
}