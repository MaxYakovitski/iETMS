package com.mayak.iet.features.company.infra.mapping;

import com.mayak.iet.company.dto.CompanyCreateDto;
import com.mayak.iet.company.dto.CompanyDto;
import com.mayak.iet.features.company.domain.model.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lanes", ignore = true)
    @Mapping(target = "penalties", ignore = true)
    Company toEntity(CompanyCreateDto companyDto);

    CompanyDto toDto(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lanes", ignore = true)
    @Mapping(target = "penalties", ignore = true)
    void updateEntityFromDto(CompanyDto dto, @MappingTarget Company company);
}