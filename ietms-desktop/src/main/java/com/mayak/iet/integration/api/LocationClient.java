package com.mayak.iet.integration.api;

import com.mayak.iet.location.dto.LocationCreateDto;
import com.mayak.iet.location.dto.LocationDto;

import java.util.List;

public interface LocationClient {
    List<LocationDto> findAll();
    void create(LocationCreateDto dto);
    void update(LocationDto dto);
    void delete(Long id);
}