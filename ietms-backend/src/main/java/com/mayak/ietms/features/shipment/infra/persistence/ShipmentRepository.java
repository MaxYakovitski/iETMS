package com.mayak.ietms.features.shipment.infra.persistence;

import com.mayak.ietms.features.shipment.domain.enums.ShipmentStatus;
import com.mayak.ietms.features.shipment.domain.model.Shipment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Returns shipments where the given user is the author (request owner)
     * and the shipment is active on the given date
     * (plannedLoadDate ≤ date ≤ plannedDropDate).
     */
    @EntityGraph(attributePaths = {"request", "request.customer", "carrier", "timestamps"})
    @Query("""
    SELECT s
    FROM Shipment s
    WHERE s.request.authorId = :userId
    AND s.plannedLoadDate <= :date
    AND s.plannedDropDate >= :date
""")
    List<Shipment> findMyShipmentsForDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * Returns transports assigned to the given dispatcher.
     * Includes active (non-final) shipments and final ones updated today —
     * so the driver can see same-day completions and cancellations.
     */
    @EntityGraph(attributePaths = {"request", "request.customer", "carrier", "timestamps"})
    @Query("""
    SELECT s FROM Shipment s
    WHERE s.dispatcherId = :userId
    AND (s.status NOT IN :finalStatuses OR s.updatedAt >= :startOfDay)
""")
    List<Shipment> findMyActiveTransports(
            @Param("userId") Long userId,
            @Param("finalStatuses") List<ShipmentStatus> finalStatuses,
            @Param("startOfDay") Instant startOfDay);

    List<Shipment> findByStatusAndPlannedLoadDateLessThanEqual(ShipmentStatus status, LocalDate date);
    List<Shipment> findByStatusAndPlannedDropDateLessThanEqual(ShipmentStatus status, LocalDate date);

    boolean existsByRequestId(Long requestId);
    boolean existsByCarrier_Id(Long carrierId);

}