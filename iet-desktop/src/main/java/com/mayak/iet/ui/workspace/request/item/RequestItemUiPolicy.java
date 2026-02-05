package com.mayak.iet.ui.workspace.request.item;

import com.mayak.iet.request.dto.enums.RequestStatusDto;
import com.mayak.iet.request.dto.view.RequestDetailsDto;
import com.mayak.iet.user.dto.UserResponseDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RequestItemUiPolicy {

    public static boolean canShowAccept(RequestDetailsDto dto, UserResponseDto user) {
        return isAuthor(dto, user)
                && dto.status() == RequestStatusDto.OFFERED;
    }

    public static boolean canShowConfirm(RequestDetailsDto dto, UserResponseDto user) {
        return isAuthor(dto, user)
                && dto.status() == RequestStatusDto.BIDDING;
    }

    public static boolean canJoin(RequestDetailsDto dto, UserResponseDto user) {
        return !isAuthor(dto, user)
                && dto.canJoin()
                && dto.status().ordinal() < RequestStatusDto.ACCEPTED.ordinal();
    }

    public static boolean canShowJoinButton(RequestDetailsDto dto, UserResponseDto user) {
        if (dto == null || user == null) return false;
        return canJoin(dto, user) || dto.isJoined();
    }

    private static boolean isAuthor(RequestDetailsDto dto, UserResponseDto user) {
        return dto.author() != null && dto.author().id().equals(user.id());
    }
}
