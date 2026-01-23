package com.mayak.iet.features.location.api;

import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.features.location.application.LocationCommandService;
import com.mayak.iet.features.location.application.LocationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationQueryService locationQueryService;
    private final LocationCommandService locationCommandService;

    // -------- READ --------

    @GetMapping
    public List<LocationDto> findAll() {
        return locationQueryService.findAll();
    }

    // -------- CREATE --------

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_LOCATIONS')")
    public void create(@RequestBody LocationDto dto) {
        locationCommandService.add(dto);
    }

    // -------- UPDATE --------

    @PutMapping
    @PreAuthorize("hasAuthority('MANAGE_LOCATIONS')")
    public void update(@RequestBody LocationDto dto) {
        locationCommandService.update(dto);
    }

    // -------- DELETE --------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_LOCATIONS')")
    public void delete(@PathVariable("id") Long id) {
        locationCommandService.delete(id);
    }
}