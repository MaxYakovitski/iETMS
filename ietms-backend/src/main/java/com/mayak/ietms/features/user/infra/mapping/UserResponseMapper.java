package com.mayak.ietms.features.user.infra.mapping;

import com.mayak.ietms.user.dto.UserLookupDto;
import com.mayak.ietms.user.dto.UserNameDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.features.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserResponseMapper {

    @Mapping(target = "profile.departmentId", source = "profile.department.id")
    @Mapping(target = "profile.departmentName", source = "profile.department.name")
    @Mapping(target = "profile.role", source = "profile.role")
    @Mapping(target = "profile.priority", source = "profile.priority")
    UserResponseDto toDto(User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "user")
    UserLookupDto toShortDto(User user);

    default UserNameDto map(User user) {
        return new UserNameDto(user.getName(), user.getSurname());
    }

}
