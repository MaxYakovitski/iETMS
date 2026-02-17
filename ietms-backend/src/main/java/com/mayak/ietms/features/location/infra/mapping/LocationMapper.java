package com.mayak.ietms.features.location.infra.mapping;

import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.features.location.domain.model.Location;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationDto dto);

    LocationDto toDto(Location location);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(LocationDto dto, @MappingTarget Location location);
}