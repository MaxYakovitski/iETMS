package com.mayak.iet.integration.rest;

import com.mayak.iet.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.iet.integration.api.ShipmentClient;
import com.mayak.iet.shipment.dto.command.CancelShipmentRequest;
import com.mayak.iet.shipment.dto.enums.ShipmentCancelReasonDto;
import com.mayak.iet.shipment.dto.view.MyTransportEventDto;
import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.shipment.dto.view.ShipmentUpdateDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShipmentRestClient extends AbstractRestClient implements ShipmentClient {

    private static final String API = "/api/shipments";

    public ShipmentRestClient(RestTemplate restTemplate, BackendConnectionMonitor connectionMonitor) {
        super(restTemplate, connectionMonitor);
    }

    @Override
    public List<ShipmentListItemDto> findMyShipments(LocalDate date) {
        return exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .get(API + "/my-shipments?date={date}", date)
                    .build();

            var body = restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<List<ShipmentListItemDto>>() {}
            ).getBody();

            return body != null ? body : List.of();
        });
    }

    @Override
    public List<MyTransportEventDto> findMyTransportEvents(LocalDate date) {
        return exchangeSafely(() -> {

            RequestEntity<Void> request = RequestEntity
                    .get(API + "/my-transport-events?date={date}", date)
                    .build();

            var body = restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<List<MyTransportEventDto>>() {}
            ).getBody();

            return body != null ? body : List.of();
        });
    }

    @Override
    public ShipmentListItemDto update(ShipmentUpdateDto dto) {
        return exchangeSafely(() -> {

            RequestEntity<ShipmentUpdateDto> request = RequestEntity
                    .patch(API + "/{id}", dto.shipmentId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);

            return restTemplate.exchange(
                    request,
                    ShipmentListItemDto.class
            ).getBody();
        });
    }

    @Override
    public void cancel(long shipmentId, ShipmentCancelReasonDto reason) {
        exchangeSafely(() -> {
            RequestEntity<CancelShipmentRequest> request =
                    RequestEntity
                            .post(API + "/{id}/cancel", shipmentId)
                            .body(new CancelShipmentRequest(reason));

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }
}