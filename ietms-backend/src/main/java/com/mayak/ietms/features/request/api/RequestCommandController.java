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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Requests", description = "Freight request lifecycle management")
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
    @Operation(summary = "Accept request", description = "For spot requests, clientPrice in the body is optional.")
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
    public void updateTid(@PathVariable("id") long id, @RequestBody UpdateTidRequest request, @CurrentUserId Long userId) {
        requestCommandService.updateTid(id, request.tid(), userId);
    }

    @PostMapping("/{id}/expire")
    @Operation(summary = "Manually expire request",
               description = "Replicates the scheduled daily expiry job. Refuses the request with reason BID_NOT_PROVIDED. " +
                       "Only the request author or an admin may call this.")
    public void expire(@PathVariable("id") long id, @CurrentUserId Long userId) {
        requestCommandService.expire(id, userId);
    }
}