package com.mayak.iet.features.bid.infra.mapping;

import com.mayak.iet.request.dto.bid.BidViewDto;
import com.mayak.iet.features.bid.domain.model.Bid;
import com.mayak.iet.features.user.infra.mapping.UserResponseMapper;
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