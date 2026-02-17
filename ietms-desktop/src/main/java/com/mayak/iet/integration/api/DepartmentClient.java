package com.mayak.iet.integration.api;

import com.mayak.iet.department.dto.DepartmentCreateDto;
import com.mayak.iet.department.dto.DepartmentDto;

import java.util.List;

public interface DepartmentClient {

    List<DepartmentDto> findAll();
    void create(DepartmentCreateDto dto);
    void update(DepartmentDto dto);
    void delete(long id);
}