package com.mayak.ietms.features.department.infra.mapping;

import com.mayak.ietms.department.dto.DepartmentDto;
import com.mayak.ietms.features.department.domain.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepartmentUpdateMapper {

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(DepartmentDto dto,
                             @MappingTarget Department department);
}