package com.mayak.iet.ui.workspace.request.item;

import com.mayak.iet.request.dto.enums.RequestStatusDto;
import com.mayak.iet.request.dto.view.RequestDetailsDto;
import com.mayak.iet.user.dto.UserResponseDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RequestItemUiPolicy {

    // ----------- ROLE -----------
    public static boolean isAuthor(RequestDetailsDto dto, UserResponseDto user) {
        return dto != null
                && user != null
                && dto.author() != null
                && dto.author().id().equals(user.id());
    }

    // ----------- JOIN -----------
    public static boolean canShowJoinButton(RequestDetailsDto dto, UserResponseDto user) {
        if (dto == null || user == null) return false;
        if (isAuthor(dto, user)) return false;
        if (dto.status() == null) return false;

        boolean notFinished = dto.status().ordinal() < RequestStatusDto.ACCEPTED.ordinal();
        return dto.isJoined() || (dto.canJoin() && notFinished);
    }

    // ----------- BID -----------
    public static boolean canBid(RequestDetailsDto dto) {
        return dto != null && dto.canBid();
    }

    // ----------- AUTHOR ACTIONS -----------
    public static boolean canConfirm(RequestDetailsDto dto, UserResponseDto user) {
        return isAuthor(dto, user) && dto.status() == RequestStatusDto.BIDDING;
    }

    public static boolean canAccept(RequestDetailsDto dto, UserResponseDto user) {
        return isAuthor(dto, user) && dto.status() == RequestStatusDto.OFFERED;
    }

    public static boolean canRefuse(RequestDetailsDto dto, UserResponseDto user) {
        return isAuthor(dto, user) && dto.status() == RequestStatusDto.OFFERED;
    }

    public static boolean canEdit(RequestDetailsDto dto, UserResponseDto user) {
        return isAuthor(dto, user);
    }

    public static boolean canDelete(RequestDetailsDto dto, UserResponseDto user) {
        return isAuthor(dto, user);
    }
}
