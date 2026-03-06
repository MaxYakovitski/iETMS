package com.mayak.ietms.features.company.infra.mapping;

import com.mayak.ietms.company.dto.CompanyCreateDto;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.features.company.domain.model.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lanes", ignore = true)
    @Mapping(target = "penalties", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Company toEntity(CompanyCreateDto companyDto);

    CompanyDto toDto(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lanes", ignore = true)
    @Mapping(target = "penalties", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CompanyDto dto, @MappingTarget Company company);
}