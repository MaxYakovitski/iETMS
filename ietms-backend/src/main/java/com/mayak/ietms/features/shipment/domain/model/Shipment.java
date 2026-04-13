package com.mayak.ietms.features.shipment.domain.model;

import com.mayak.ietms.features.company.domain.model.Company;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.shipment.domain.enums.ShipmentCancelReason;
import com.mayak.ietms.features.shipment.domain.enums.ShipmentStatus;
import com.mayak.ietms.features.shipment.domain.enums.TransitionInitiator;
import com.mayak.ietms.shared.exception.business.DeliveryTimeLineException;
import com.mayak.ietms.shared.exception.business.InvalidShipmentStatusTransitionException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Core domain entity representing a shipment in execution.
 *
 * <p>A shipment is created from a {@link Request} and progresses through
 * a defined lifecycle: NEW → PLANNED → TO_LOAD → LOADED → TO_DROP → DROPPED.
 * It may be CANCELED at any non-final stage by an authorized user.
 *
 * <p>Status transitions are enforced via {@link ShipmentStatus#canTransitionTo}.
 * Timeline timestamps are recorded for auditable statuses only.
 */
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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public Shipment(Request request) {
        this.request = request;
        this.status = ShipmentStatus.NEW;
        addTimestamp(ShipmentStatus.NEW, Instant.now());
        this.plannedLoadDate = request.getStartDate().toLocalDate();
        this.plannedDropDate = request.getEndDate().toLocalDate();
    }

    public void markPlanned(Instant at) {
        if (!status.canTransitionTo(ShipmentStatus.PLANNED, TransitionInitiator.USER)) {
            throw new InvalidShipmentStatusTransitionException(status, ShipmentStatus.PLANNED);
        }
        Instant newTimestamp = getLastTimestamp(ShipmentStatus.NEW);
        if (newTimestamp != null && at.isBefore(newTimestamp)) {
            throw new DeliveryTimeLineException("Actual planning time cannot be before shipment creation time!");
        }
        this.status = ShipmentStatus.PLANNED;
        addTimestamp(ShipmentStatus.PLANNED, at);
    }

    /**
     * Transitions the shipment to TO_LOAD by a user action.
     *
     * <p>If the current status is NEW (i.e. the shipment was never explicitly planned),
     * a PLANNED timestamp is recorded automatically at the current time
     * to preserve a complete timeline.
     */
    public void markToLoadByUser() {
        if (!status.canTransitionTo(ShipmentStatus.TO_LOAD, TransitionInitiator.USER)) {
            throw new InvalidShipmentStatusTransitionException(status, ShipmentStatus.TO_LOAD);
        }
        if (this.status == ShipmentStatus.NEW) {
            addTimestamp(ShipmentStatus.PLANNED, Instant.now());
        }
        this.status = ShipmentStatus.TO_LOAD;
    }

    public void markToLoadBySystem() {
        if (!status.canTransitionTo(ShipmentStatus.TO_LOAD, TransitionInitiator.SYSTEM)) {
            throw new InvalidShipmentStatusTransitionException(status, ShipmentStatus.TO_LOAD);
        }
        this.status = ShipmentStatus.TO_LOAD;
    }

    /**
     * Reverts the shipment to NEW status, removing all PLANNED timestamps.
     *
     * <p>Used when transport assignment is canceled and the shipment
     * needs to be re-planned from scratch.
     */
    public void revertToNew() {
        if (!status.canTransitionTo(ShipmentStatus.NEW, TransitionInitiator.USER)) {
            throw new InvalidShipmentStatusTransitionException(status, ShipmentStatus.NEW);
        }
        timestamps.removeIf(t -> t.getStatus() == ShipmentStatus.PLANNED);
        this.status = ShipmentStatus.NEW;
    }

    /**
     * Marks the shipment as loaded at the given time.
     *
     * @param at actual loading time — must not be before the PLANNED timestamp
     */
    public void markLoaded(Instant at) {
        if (!status.canTransitionTo(ShipmentStatus.LOADED, TransitionInitiator.USER)) {
            throw new InvalidShipmentStatusTransitionException(status, ShipmentStatus.LOADED);
        }
        Instant plannedTimestamp = getLastTimestamp(ShipmentStatus.PLANNED);
        if (plannedTimestamp != null && at.isBefore(plannedTimestamp)) {
            throw new DeliveryTimeLineException("Actual loading time cannot be before planned loading time!");
        }
        this.status = ShipmentStatus.LOADED;
        addTimestamp(ShipmentStatus.LOADED, at);
    }

    public void markToDropByUser() {
        if (!status.canTransitionTo(ShipmentStatus.TO_DROP, TransitionInitiator.USER)) {
            throw new InvalidShipmentStatusTransitionException(status, ShipmentStatus.TO_DROP);
        }
        this.status = ShipmentStatus.TO_DROP;
    }

    public void markToDropBySystem() {
        if (!status.canTransitionTo(ShipmentStatus.TO_DROP, TransitionInitiator.SYSTEM)) {
            throw new InvalidShipmentStatusTransitionException(status, ShipmentStatus.TO_DROP);
        }
        this.status = ShipmentStatus.TO_DROP;
    }

    /**
     * Marks the shipment as dropped (delivered) at the given time.
     *
     * @param at actual delivery time — must not be before the LOADED timestamp
     */
    public void markDropped(Instant at) {
        if (!status.canTransitionTo(ShipmentStatus.DROPPED, TransitionInitiator.USER)) {
            throw new InvalidShipmentStatusTransitionException(status, ShipmentStatus.DROPPED);
        }
        Instant loadedTimestamp = getLastTimestamp(ShipmentStatus.LOADED);
        if (loadedTimestamp == null) {
            throw new DeliveryTimeLineException("Cannot drop shipment that was not loaded!");
        }
        if (at.isBefore(loadedTimestamp)) {
            throw new DeliveryTimeLineException("Dropping time cannot be before loading time!");
        }
        this.status = ShipmentStatus.DROPPED;
        addTimestamp(ShipmentStatus.DROPPED, at);
    }

    public void cancel(ShipmentCancelReason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("Cancel reason must not be null!");
        }

        if (!status.canTransitionTo(ShipmentStatus.CANCELED, TransitionInitiator.USER)) {
            throw new InvalidShipmentStatusTransitionException(status, ShipmentStatus.CANCELED);
        }

        this.status = ShipmentStatus.CANCELED;
        this.cancelReason = reason;
        addTimestamp(ShipmentStatus.CANCELED, Instant.now());
    }

    public void assignDispatcher(Long dispatcherId) {
        if (dispatcherId == null) throw new IllegalArgumentException("Dispatcher id must not be null!");
        this.dispatcherId = dispatcherId;
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

    public boolean isOwnedBy(Long userId) {
        return request != null && request.getAuthorId().equals(userId);
    }

    public boolean isDispatchedBy(Long userId) {
        return dispatcherId != null && dispatcherId.equals(userId);
    }

    public Set<Long> collectParticipantIds() {
        Set<Long> ids = new HashSet<>();
        if (request != null && request.getAuthorId() != null) {
            ids.add(request.getAuthorId());
        }
        if (dispatcherId != null) {
            ids.add(dispatcherId);
        }
        return ids;
    }

    private void addTimestamp(ShipmentStatus status, Instant at) {
        if (status == null) throw new IllegalArgumentException("Status must not be null!");
        if (at == null) throw new IllegalArgumentException("Timestamp time must not be null!");
        if (!status.hasTimestamp()) throw new IllegalArgumentException("Status " + status + " does not record a timestamp!");
        this.timestamps.add(new ShipmentTimeStamp(this, status, at));
    }

    private Instant getLastTimestamp(ShipmentStatus status) {
        return timestamps.stream()
                .filter(t -> t.getStatus() == status)
                .map(ShipmentTimeStamp::getAt)
                .max(Instant::compareTo)
                .orElse(null);
    }
}