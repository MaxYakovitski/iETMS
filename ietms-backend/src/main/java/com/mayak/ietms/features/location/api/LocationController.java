package com.mayak.ietms.features.location.api;

import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.features.location.application.LocationCommandService;
import com.mayak.ietms.features.location.application.LocationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Locations", description = "Delivery location management")
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
    @Operation(summary = "Update location",
               description = "Location id is passed in the request body, not in the path.")
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