package com.mayak.ietms.integration.api;

import com.mayak.ietms.lane.dto.LaneCreateDto;
import com.mayak.ietms.lane.dto.LaneViewDto;

import java.util.List;

public interface LaneClient {

    List<LaneViewDto> findByCompany(long companyId);

    LaneViewDto create(long companyId, LaneCreateDto dto);
    LaneViewDto update(long laneId, LaneCreateDto dto);
    void delete(long laneId);
}