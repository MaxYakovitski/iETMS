package com.mayak.ietms.features.request.api;

import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.request.dto.command.AcceptRequest;
import com.mayak.ietms.request.dto.command.RefuseRequest;
import com.mayak.ietms.request.dto.command.UpdateTidRequest;
import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.request.application.assembly.RequestDetailsAssembler;
import com.mayak.ietms.infrastructure.security.current.CurrentUserId;
import com.mayak.ietms.features.request.application.RequestCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestCommandController {

    private final RequestCommandService requestCommandService;
    private final UserQueryService userQueryService;
    private final RequestDetailsAssembler requestDetailsAssembler;

    @PostMapping
    public RequestDetailsDto create(@RequestBody BaseRequestDto dto, @CurrentUserId Long userId) {
        var request = requestCommandService.create(dto, userId);
        User actor = userQueryService.getEntityById(userId);
        return requestDetailsAssembler.toDto(request, actor, Set.of());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") long id,  @CurrentUserId Long userId) {
        requestCommandService.delete(id, userId);
    }

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void join(@PathVariable("id") long id, @CurrentUserId Long userId) {
        requestCommandService.join(id, userId);
    }

    @PostMapping("/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable("id") long id,  @CurrentUserId Long userId) {
        requestCommandService.leave(id, userId);
    }

    @PostMapping("/{id}/offer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void offer(@PathVariable("id") long id,   @CurrentUserId Long userId) {
        requestCommandService.offer(id, userId);
    }

    @PostMapping("/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptSpot(@PathVariable("id") long id,
            @RequestBody(required = false) AcceptRequest request,
            @CurrentUserId Long userId) {
        BigDecimal price = request != null ? request.clientPrice() : null;
        requestCommandService.accept(id, price, userId);
    }

    @PostMapping("/{id}/refuse")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refuse(@PathVariable("id") long id, @RequestBody RefuseRequest request, @CurrentUserId Long userId) {
        requestCommandService.refuse(id, request.reason(), userId);
    }

    @PatchMapping("/{id}/tid")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTid(
            @PathVariable("id") long id,
            @RequestBody UpdateTidRequest request,
            @CurrentUserId Long userId) {
        requestCommandService.updateTid(id, request.tid(), userId);
    }

    /**
     * Manually expires the request by refusing it with reason {@code BID_NOT_PROVIDED},
     * replicating the behaviour of the scheduled daily expiry job.
     * Only the request author or an admin may call this endpoint.
     */
    @PostMapping("/{id}/expire")
    public void expire(@PathVariable("id") long id, @CurrentUserId Long userId) {
        requestCommandService.expire(id, userId);
    }
}