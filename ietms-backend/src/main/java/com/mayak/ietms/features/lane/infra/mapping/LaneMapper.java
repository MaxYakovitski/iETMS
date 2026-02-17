package com.mayak.ietms.features.lane.infra.mapping;

import com.mayak.ietms.lane.dto.LaneCreateDto;
import com.mayak.ietms.lane.dto.LaneViewDto;
import com.mayak.ietms.features.lane.domain.model.Lane;
import com.mayak.ietms.features.location.infra.mapping.LocationMapper;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = LocationMapper.class)
public interface LaneMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "linkedLane", ignore = true)
    @Mapping(target = "fromLocation", ignore = true)
    @Mapping(target = "toLocation", ignore = true)
    Lane toEntity(LaneCreateDto dto);

    @Mapping(target = "totalPrice", source = "lane", qualifiedByName = "laneTotalPrice")
    LaneViewDto toViewDto(Lane lane);

    @Named("laneTotalPrice")
    default BigDecimal laneTotalPrice(Lane lane) {
        return lane != null ? lane.getTotalPrice() : null;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "linkedLane", ignore = true)
    @Mapping(target = "fromLocation", ignore = true)
    @Mapping(target = "toLocation", ignore = true)
    void updateEntity(@MappingTarget Lane lane, LaneCreateDto dto);
}