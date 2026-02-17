package com.mayak.ietms.integration.api;

import com.mayak.ietms.department.dto.DepartmentCreateDto;
import com.mayak.ietms.department.dto.DepartmentDto;

import java.util.List;

public interface DepartmentClient {

    List<DepartmentDto> findAll();
    void create(DepartmentCreateDto dto);
    void update(DepartmentDto dto);
    void delete(long id);
}