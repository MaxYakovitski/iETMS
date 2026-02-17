package com.mayak.iet.features.request.infra.mapping;

import com.mayak.iet.features.request.domain.model.RefuseReason;
import com.mayak.iet.request.dto.create.ContractRequestDto;
import com.mayak.iet.features.request.domain.model.ContractRequest;
import com.mayak.iet.features.bid.infra.mapping.BidMapper;
import com.mayak.iet.features.company.infra.mapping.CompanyMapper;
import com.mayak.iet.features.location.infra.mapping.LocationMapper;
import com.mayak.iet.features.user.infra.mapping.UserResponseMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {
        UserResponseMapper.class,
        BidMapper.class,
        CompanyMapper.class,
        LocationMapper.class,
})
public interface ContractRequestMapper {

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
    @Mapping(target = "lane", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "issueDate", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "clientPrice", ignore = true)
    @Mapping(target = "bidPrice", ignore = true)
    @Mapping(target = "profitMargin", ignore = true)
    @Mapping(target = "competitorsId", ignore = true)
    @Mapping(target = "dispatcherId", ignore = true)
    @Mapping(target = "bids", ignore = true)
    @Mapping(target = "fromLocationIds", ignore = true)
    @Mapping(target = "toLocationIds", ignore = true)
    ContractRequest toEntity(ContractRequestDto dto, @Context RefuseReasonMapper reasonMapper);


    @AfterMapping
    default void afterCreate(
            ContractRequestDto dto,
            @MappingTarget ContractRequest entity,
            @Context RefuseReasonMapper reasonMapper
    ) {
        RefuseReason reason = reasonMapper.fromContractDto(dto.getReasonCode());
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
    @Mapping(target = "lane", ignore = true)
    @Mapping(target = "fromLocationIds", ignore = true)
    @Mapping(target = "toLocationIds", ignore = true)
    void updateEntity(
            @MappingTarget ContractRequest entity,
            ContractRequestDto dto,
            @Context RefuseReasonMapper reasonMapper);


    @AfterMapping
    default void afterUpdate(
            ContractRequestDto dto,
            @MappingTarget ContractRequest entity,
            @Context RefuseReasonMapper reasonMapper
    ) {
        RefuseReason reason = reasonMapper.fromContractDto(dto.getReasonCode());
        if (reason != null) {
            entity.setReason(reason);
        }
    }

}