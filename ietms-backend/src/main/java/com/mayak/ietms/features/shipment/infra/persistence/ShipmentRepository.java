package com.mayak.ietms.features.shipment.infra.persistence;

import com.mayak.ietms.features.shipment.domain.model.Shipment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    @EntityGraph(attributePaths = {"request", "request.customer", "carrier", "timestamps"})
    @Query("""
    SELECT s
    FROM Shipment s
    WHERE s.request.authorId = :userId
    AND s.plannedLoadDate <= :date
    AND s.plannedDropDate >= :date
""")
    List<Shipment> findMyShipmentsForDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @EntityGraph(attributePaths = {"request", "request.customer", "carrier", "timestamps"})
    @Query("""
    SELECT s
    FROM Shipment s
    WHERE s.dispatcherId = :userId
""")
    List<Shipment> findMyTransportShipments(@Param("userId") Long userId);

    boolean existsByRequestId(Long requestId);

}