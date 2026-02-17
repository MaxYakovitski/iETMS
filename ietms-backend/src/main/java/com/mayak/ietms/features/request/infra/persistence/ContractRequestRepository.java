package com.mayak.ietms.features.request.infra.persistence;

import com.mayak.ietms.features.request.domain.model.ContractRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRequestRepository extends JpaRepository<ContractRequest, Long> {
    boolean existsByLane_Id(Long laneId);
}
