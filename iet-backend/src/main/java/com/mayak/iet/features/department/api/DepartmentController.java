package com.mayak.iet.features.department.api;

import com.mayak.iet.department.dto.DepartmentCreateDto;
import com.mayak.iet.department.dto.DepartmentDto;
import com.mayak.iet.features.department.application.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public List<DepartmentDto> findAll() {
        return departmentService.findAll()
                .stream()
                .map(d -> new DepartmentDto(d.getId(), d.getName(), d.getCode()))
                .toList();
    }

    // -------- CREATE --------

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENTS')")
    public void create(@RequestBody DepartmentCreateDto dto) {
        departmentService.create(dto);
    }

    // -------- UPDATE --------

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENTS')")
    public void update(@PathVariable("id") Long id, @RequestBody DepartmentDto dto) {
        departmentService.update(id, dto);
    }

    // -------- DELETE --------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENTS')")
    public void delete(@PathVariable("id") Long id) {
        departmentService.delete(id);
    }
}