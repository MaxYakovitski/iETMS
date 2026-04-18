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

    /**
     * Returns {@code true} if the given user is the author of the request.
     */
    public static boolean isAuthor(RequestDetailsDto dto, UserResponseDto user) {
        return dto != null
                && user != null
                && dto.author() != null
                && dto.author().id().equals(user.id());
    }

    /**
     * Returns {@code true} if the join/unjoin button should be visible.
     * Hidden for the request author. Visible if the user has already joined
     * or can still join and the request is not yet finished.
     */
    public static boolean canShowJoinButton(RequestDetailsDto dto, UserResponseDto user) {
        if (dto == null || user == null) return false;
        if (isAuthor(dto, user)) return false;
        if (dto.status() == null) return false;

        boolean notFinished = dto.status().ordinal() < RequestStatusDto.ACCEPTED.ordinal();
        return dto.isJoined() || (dto.canJoin() && notFinished);
    }

    /**
     * Returns {@code true} if the user can place a bid on the request.
     */
    public static boolean canBid(RequestDetailsDto dto) {
        return dto != null && dto.canBid();
    }

    /**
     * Returns {@code true} if the author can confirm (offer) the request.
     * Only allowed when the request is in {@code BIDDING} status.
     */
    public static boolean canConfirm(RequestDetailsDto dto, UserResponseDto user) {
        return isAuthor(dto, user) && dto.status() == RequestStatusDto.BIDDING;
    }

    /**
     * Returns {@code true} if the author can accept the offered request.
     * Only allowed when the request is in {@code OFFERED} status.
     */
    public static boolean canAccept(RequestDetailsDto dto, UserResponseDto user) {
        return isAuthor(dto, user) && dto.status() == RequestStatusDto.OFFERED;
    }

    /**
     * Returns {@code true} if the author can refuse the offered request.
     * Only allowed when the request is in {@code OFFERED} status.
     */
    public static boolean canRefuse(RequestDetailsDto dto, UserResponseDto user) {
        return isAuthor(dto, user) && dto.status() == RequestStatusDto.OFFERED;
    }

    /**
     * Returns {@code true} if the author can edit (renew) the request.
     */
    public static boolean canEdit(RequestDetailsDto dto, UserResponseDto user) {
        return isAuthor(dto, user);
    }

    /**
     * Returns {@code true} if the user can delete the request.
     * Authors can delete their own requests.
     * Admins can delete any request regardless of authorship.
     */
    public static boolean canDelete(RequestDetailsDto dto, UserResponseDto user) {
        if (dto == null || user == null) return false;
        return isAuthor(dto, user) || user.userType() == UserTypeDto.ADMIN;
    }

    /**
     * Returns {@code true} if the user can manually expire the request.
     * Only allowed for requests in {@code NEW} or {@code IN_PROGRESS} status.
     * Authors can expire their own requests.
     * Admins can expire any request regardless of authorship.
     */
    public static boolean canExpire(RequestDetailsDto dto, UserResponseDto user) {
        if (dto == null || user == null) return false;
        if (dto.status() != RequestStatusDto.NEW && dto.status() != RequestStatusDto.IN_PROGRESS) return false;
        return isAuthor(dto, user) || user.userType() == UserTypeDto.ADMIN;
    }
}