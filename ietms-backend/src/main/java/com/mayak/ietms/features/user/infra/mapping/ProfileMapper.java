package com.mayak.ietms.features.user.infra.mapping;

import com.mayak.ietms.user.dto.ProfileResponseDto;
import com.mayak.ietms.features.user.domain.model.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    ProfileResponseDto toDto(Profile profile);
}