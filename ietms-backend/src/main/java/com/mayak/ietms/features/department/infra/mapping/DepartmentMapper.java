package com.mayak.ietms.features.department.infra.mapping;

import com.mayak.ietms.department.dto.DepartmentCreateDto;
import com.mayak.ietms.features.department.domain.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "id", ignore = true)
    Department toEntity(DepartmentCreateDto dto);
}