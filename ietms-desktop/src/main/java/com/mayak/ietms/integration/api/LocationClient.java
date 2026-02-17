package com.mayak.ietms.integration.api;

import com.mayak.ietms.location.dto.LocationCreateDto;
import com.mayak.ietms.location.dto.LocationDto;

import java.util.List;

public interface LocationClient {
    List<LocationDto> findAll();
    void create(LocationCreateDto dto);
    void update(LocationDto dto);
    void delete(Long id);
}