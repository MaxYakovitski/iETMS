package com.mayak.ietms.features.lane.api;

import com.mayak.ietms.lane.dto.LaneCreateDto;
import com.mayak.ietms.lane.dto.LaneViewDto;
import com.mayak.ietms.features.lane.application.LaneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lanes")
@Tag(name = "Lanes", description = "Contract lanes (recurring routes) per company")
@RequiredArgsConstructor
public class LaneController {

    private final LaneService laneService;

    @GetMapping("/by-company/{companyId}")
    @Operation(summary = "Find lanes by company")
    public List<LaneViewDto> findByCompany(@PathVariable("companyId") long companyId) {
        return laneService.findByCompany(companyId);
    }

    @PostMapping("/by-company/{companyId}")
    @Operation(summary = "Create lane for company",
               description = "Lane is always created under a specific company, identified by companyId in the path.")
    public LaneViewDto create(@PathVariable("companyId") long companyId, @RequestBody LaneCreateDto dto) {
        return laneService.create(companyId, dto);
    }

    @PutMapping("/{id}")
    public LaneViewDto update(@PathVariable("id") long id, @RequestBody LaneCreateDto dto) {
        return laneService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") long id) {
        laneService.delete(id);
    }
}