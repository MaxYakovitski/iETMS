package com.mayak.iet.features.user.infra.mapping;

import com.mayak.iet.user.dto.UserUpdateDto;
import com.mayak.iet.features.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserUpdateMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "password", ignore = true)
    void update(@MappingTarget User user, UserUpdateDto dto);
}