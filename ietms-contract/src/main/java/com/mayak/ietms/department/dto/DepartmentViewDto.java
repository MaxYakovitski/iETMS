package com.mayak.ietms.department.dto;

import java.util.List;

public record DepartmentViewDto (
        Long id,
        String name,
        String code,
        Long managerId,
        List<Long> employeesIds){
}