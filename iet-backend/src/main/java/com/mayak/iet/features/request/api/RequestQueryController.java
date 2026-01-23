package com.mayak.iet.features.request.api;

import com.mayak.iet.common.dto.page.PageDto;
import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.request.dto.filter.RequestFilterDto;
import com.mayak.iet.request.dto.view.RequestDetailsDto;
import com.mayak.iet.request.dto.view.RequestListItemDto;
import com.mayak.iet.features.user.domain.model.User;
import com.mayak.iet.infrastructure.security.current.CurrentUserId;
import com.mayak.iet.features.request.application.RequestQueryService;
import com.mayak.iet.features.user.application.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestQueryController {

    private final RequestQueryService service;
    private final UserQueryService userQueryService;

    @GetMapping
    public PageDto<RequestListItemDto> findPage(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "type", required = false) RequestTypeDto type
    ) {
        return service.findPage(page, size, type);
    }

    @GetMapping("/search")
    public PageDto<RequestListItemDto> search(
            @RequestParam("q") String query,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ) {
        return service.search(query, page, size);
    }

    @PostMapping("/filter")
    public PageDto<RequestListItemDto> filter(
            @RequestBody RequestFilterDto filter,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ) {
        return service.filter(filter, page, size);
    }

    @GetMapping("/{id}")
    public RequestDetailsDto getDetails(@PathVariable("id") long id, @CurrentUserId Long userId) {
        User actor = userId != null ? userQueryService.getEntityById(userId) : null;
        return service.getDetails(id, actor);
    }

    @GetMapping("/{id}/exchange")
    public String getExchange(@PathVariable("id") long id) {
        return service.getExchangeString(id);
    }

    @GetMapping("/{id}/has-shipment")
    public boolean hasShipment(@PathVariable("id") long id) {
        return service.hasShipment(id);
    }

}