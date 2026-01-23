package com.mayak.iet.features.lane.infra.persistence;

import com.mayak.iet.features.lane.domain.model.Lane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LaneRepository extends JpaRepository<Lane, Long> {

    @Query("""
    SELECT l FROM Lane l
    JOIN FETCH l.fromLocation
    JOIN FETCH l.toLocation
    WHERE l.customer.id = :customerId
""")
    List<Lane> findByCompanyIdWithFetch(@Param("customerId") Long customerId);

    boolean existsByLinkedLane_Id(Long linkedLaneId);
    boolean existsByCustomer_Id(Long customerId);
}