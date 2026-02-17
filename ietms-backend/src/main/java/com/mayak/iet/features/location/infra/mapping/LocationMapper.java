package com.mayak.iet.features.location.infra.mapping;

import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.features.location.domain.model.Location;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationDto dto);

    LocationDto toDto(Location location);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(LocationDto dto, @MappingTarget Location location);
}