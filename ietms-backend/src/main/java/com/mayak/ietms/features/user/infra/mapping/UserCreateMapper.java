package com.mayak.ietms.features.user.infra.mapping;

import com.mayak.ietms.user.dto.UserCreateDto;
import com.mayak.ietms.features.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserCreateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserCreateDto dto);
}