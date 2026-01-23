package com.mayak.iet.features.lane.api;

import com.mayak.iet.lane.dto.LaneCreateDto;
import com.mayak.iet.lane.dto.LaneViewDto;
import com.mayak.iet.features.lane.application.LaneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lanes")
@RequiredArgsConstructor
public class LaneController {

    private final LaneService laneService;

    @GetMapping("/by-company/{companyId}")
    public List<LaneViewDto> findByCompany(@PathVariable("companyId") long companyId) {
        return laneService.findByCompany(companyId);
    }

    @PostMapping("/by-company/{companyId}")
    public LaneViewDto create(
            @PathVariable("companyId") long companyId,
            @RequestBody LaneCreateDto dto
    ) {
        return laneService.create(companyId, dto);
    }

    @PutMapping("/{id}")
    public LaneViewDto update(
            @PathVariable("id") long id,
            @RequestBody LaneCreateDto dto
    ) {
        return laneService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") long id) {
        laneService.delete(id);
    }
}