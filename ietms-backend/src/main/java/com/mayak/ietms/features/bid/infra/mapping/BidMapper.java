package com.mayak.ietms.features.bid.infra.mapping;

import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.features.bid.domain.model.Bid;
import com.mayak.ietms.features.user.infra.mapping.UserResponseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring", uses = UserResponseMapper.class)
public interface BidMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "user", source = "user")
    BidViewDto toViewDto(Bid bid);

    Set<BidViewDto> toViewDtoSet(Set<Bid> bids);
}