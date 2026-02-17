package com.mayak.iet.features.request.infra.mapping;

import com.mayak.iet.request.dto.create.SpotRequestDto;
import com.mayak.iet.features.request.domain.model.SpotRequest;
import com.mayak.iet.features.bid.infra.mapping.BidMapper;
import com.mayak.iet.features.company.infra.mapping.CompanyMapper;
import com.mayak.iet.features.location.infra.mapping.LocationMapper;
import com.mayak.iet.features.user.infra.mapping.UserResponseMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {
        UserResponseMapper.class,
        BidMapper.class,
        CompanyMapper.class,
        LocationMapper.class
})
public interface SpotRequestMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "customerReference", source = "customerReference")

    @Mapping(target = "customer", ignore = true)

    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")

    @Mapping(target = "shipmentType", source = "shipmentType")
    @Mapping(target = "transportType", source = "transportType")

    @Mapping(target = "dangerous", source = "dangerous")
    @Mapping(target = "temperature", source = "temperature")
    @Mapping(target = "weight", source = "weight")
    @Mapping(target = "loadingMeter", source = "loadingMeter")

    @Mapping(target = "comments", source = "comments")
    // --- system / derived ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tid", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "issueDate", ignore = true)
    @Mapping(target = "clientPrice", ignore = true)
    @Mapping(target = "bidPrice", ignore = true)
    @Mapping(target = "profitMargin", ignore = true)
    @Mapping(target = "competitorsId", ignore = true)
    @Mapping(target = "dispatcherId", ignore = true)
    @Mapping(target = "bids", ignore = true)
    @Mapping(target = "fromLocationIds", ignore = true)
    @Mapping(target = "toLocationIds", ignore = true)
    SpotRequest toEntity(SpotRequestDto dto, @Context RefuseReasonMapper reasonMapper);

    @AfterMapping
    default void afterCreate(
            SpotRequestDto dto,
            @MappingTarget SpotRequest entity,
            @Context RefuseReasonMapper reasonMapper
    ) {
        var reason = reasonMapper.fromSpotDto(dto.getReasonCode());
        if (reason != null) {
            entity.setReason(reason);
        }
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "customerReference", source = "customerReference")

    @Mapping(target = "customer", ignore = true)

    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")

    @Mapping(target = "shipmentType", source = "shipmentType")
    @Mapping(target = "transportType", source = "transportType")

    @Mapping(target = "dangerous", source = "dangerous")
    @Mapping(target = "temperature", source = "temperature")
    @Mapping(target = "weight", source = "weight")
    @Mapping(target = "loadingMeter", source = "loadingMeter")

    @Mapping(target = "comments", source = "comments")
    // system fields
    @Mapping(target = "fromLocationIds", ignore = true)
    @Mapping(target = "toLocationIds", ignore = true)
    void updateEntity(@MappingTarget SpotRequest entity, SpotRequestDto dto);

    @AfterMapping
    default void afterUpdate(
            SpotRequestDto dto,
            @MappingTarget SpotRequest entity,
            @Context RefuseReasonMapper reasonMapper
    ) {
        var reason = reasonMapper.fromSpotDto(dto.getReasonCode());
        if (reason != null) {
            entity.setReason(reason);
        }
    }
}