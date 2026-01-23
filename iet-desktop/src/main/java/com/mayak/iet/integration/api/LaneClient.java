package com.mayak.iet.integration.api;

import com.mayak.iet.lane.dto.LaneCreateDto;
import com.mayak.iet.lane.dto.LaneViewDto;

import java.util.List;

public interface LaneClient {

    List<LaneViewDto> findByCompany(long companyId);

    LaneViewDto create(long companyId, LaneCreateDto dto);
    LaneViewDto update(long laneId, LaneCreateDto dto);
    void delete(long laneId);
}