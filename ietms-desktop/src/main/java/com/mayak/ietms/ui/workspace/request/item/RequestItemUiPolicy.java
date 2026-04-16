package com.mayak.ietms.ui.workspace.request.item;

import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.user.dto.enums.UserTypeDto;
import lombok.NoArgsConstructor;

/**
 * Determines UI button visibility for a request list item
 * based on the current user's role and request state.
 */
@NoArgsConstructor
public class RequestItemUiPolicy {

    // ----------- PREDICATES -----------
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

    /**
     * Authors can delete their own requests.
     * Admins can delete any request regardless of authorship.
     */
    public static boolean canDelete(RequestDetailsDto dto, UserResponseDto user) {
        if (dto == null || user == null) return false;
        return isAuthor(dto, user) || user.userType() == UserTypeDto.ADMIN;
    }
}
