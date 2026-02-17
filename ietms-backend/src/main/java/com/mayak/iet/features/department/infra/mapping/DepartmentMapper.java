package com.mayak.iet.features.department.infra.mapping;

import com.mayak.iet.department.dto.DepartmentCreateDto;
import com.mayak.iet.features.department.domain.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "id", ignore = true)
    Department toEntity(DepartmentCreateDto dto);
}