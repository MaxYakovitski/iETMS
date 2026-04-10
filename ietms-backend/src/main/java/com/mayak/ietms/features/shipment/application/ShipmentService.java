package com.mayak.ietms.features.shipment.application;

import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.features.shipment.application.notify.ShipmentNotificationService;
import com.mayak.ietms.features.shipment.application.assembly.ShipmentListItemAssembler;
import com.mayak.ietms.shared.exception.business.*;
import com.mayak.ietms.shared.exception.validation.ValidationException;
import com.mayak.ietms.shipment.dto.enums.TransportEventType;
import com.mayak.ietms.shipment.dto.view.MyTransportEventDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.shipment.dto.view.ShipmentUpdateDto;
import com.mayak.ietms.features.company.domain.model.Company;
import com.mayak.ietms.features.shipment.domain.model.Shipment;
import com.mayak.ietms.features.shipment.domain.enums.ShipmentCancelReason;
import com.mayak.ietms.features.shipment.domain.enums.ShipmentStatus;
import com.mayak.ietms.shipment.event.ShipmentEvent;
import com.mayak.ietms.features.shipment.infra.persistence.ShipmentRepository;
import com.mayak.ietms.features.company.application.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

/**
 * Application service responsible for shipment-related use cases.
 *
 * <p>
 * This service orchestrates domain operations on {@link Shipment} entities,
 * enforces authorization rules, and coordinates data projection via
 * {@link ShipmentListItemAssembler}.
 * </p>
 *
 * <p>
 * Business rules related to shipment state transitions are delegated
 * to the domain model, while this service ensures correct invocation
 * order and transactional boundaries.
 * </p>
 */

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final CompanyService companyService;
    private final ShipmentListItemAssembler assembler;
    private final ShipmentNotificationService shipmentNotificationService;

    /**
     * Returns shipments visible to the given user for the specified calendar date.
     *
     * <p>
     * A shipment is considered visible if the date falls within its planned
     * execution window, with special handling for canceled shipments.
     * </p>
     *
     * @param date   calendar date selected by the user
     * @param userId identifier of the requesting user
     * @return list of shipment projections as of the given date
     */
    public List<ShipmentListItemDto> findMyShipmentsForDate(LocalDate date, Long userId) {
        return shipmentRepository
                .findMyShipmentsForDate(userId,  date)
                .stream()
                .map(s -> assembler.assembleForPlanner(s, date))
                .toList();
    }

    /**
     * Returns detailed shipment projection for the given user.
     *
     * <p>
     * Access is granted only if the user is either:
     * <ul>
     *     <li>the shipment owner (request author), or</li>
     *     <li>the assigned dispatcher</li>
     * </ul>
     * </p>
     *
     * @param shipmentId shipment identifier
     * @param userId authenticated user identifier
     * @return current shipment projection
     * @throws UnauthorizedException if user is not authenticated or has no access
     */
    @Transactional(readOnly = true)
    public ShipmentListItemDto getDetails(Long shipmentId, Long userId) {
        if (userId == null) throw new UnauthorizedException("Not authenticated!");

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        if (!shipment.isOwnedBy(userId) && !shipment.isDispatchedBy(userId)) {
            throw new UnauthorizedException("No access!");
        }

        return assembler.assembleCurrent(shipment);
    }

    /**
     * Returns transport events (LOAD / DROP) assigned to the given dispatcher
     * for the specified calendar date.
     *
     * <p>
     * The method retrieves shipments where the user acts as dispatcher and
     * projects them into date-specific transport events.
     * Depending on shipment state and planned dates, a shipment may produce:
     * <ul>
     *     <li>a LOAD event,</li>
     *     <li>a DROP event,</li>
     *     <li>or no events at all.</li>
     * </ul>
     * </p>
     *
     * <p>
     * The projection reflects shipment state as of the given date.
     * </p>
     *
     * @param date   calendar date selected in planner view
     * @param userId dispatcher identifier
     * @return list of transport events for the date
     */
    public List<MyTransportEventDto> findMyTransportEventsForDate(LocalDate date, Long userId) {
        return shipmentRepository.findMyTransportShipments(userId).stream()
                .flatMap(s -> toTransportEvents(s, date))
                .toList();
    }

    /**
     * Projects a shipment into transport events for the given calendar date.
     *
     * <p>
     * Depending on shipment status and planned dates, this method may produce
     * LOAD and/or DROP transport events, or no events at all.
     * </p>
     *
     * <p>
     * The returned projection reflects the shipment state as of the given date.
     * </p>
     *
     * @param s    shipment entity
     * @param date calendar date
     * @return stream of transport events applicable for the date
     */
    private Stream<MyTransportEventDto> toTransportEvents(Shipment s, LocalDate date) {
        ShipmentListItemDto dto = assembler.assembleAsOfDate(s, date);

        if (s.getStatus() == ShipmentStatus.CANCELED) {
            if (!date.equals(s.getPlannedLoadDate())) return Stream.empty();
            return Stream.of(new MyTransportEventDto(
                        s.getId(),
                        TransportEventType.LOAD,
                        s.getPlannedLoadDate().atStartOfDay(),
                        dto
                ));
        }

        Stream.Builder<MyTransportEventDto> b = Stream.builder();

        if (date.equals(s.getPlannedLoadDate())) {
            b.add(new MyTransportEventDto(
                    s.getId(),
                    TransportEventType.LOAD,
                    s.getPlannedLoadDate().atStartOfDay(),
                    dto
            ));
        }

        if (date.equals(s.getPlannedDropDate()) && s.isLoadedBeforeOrOn(date)) {
            b.add(new MyTransportEventDto(
                    s.getId(),
                    TransportEventType.DROP,
                    s.getPlannedDropDate().atStartOfDay(),
                    dto
            ));
        }

        return b.build();
    }

    /**
     * Updates shipment data and applies an optional status transition.
     *
     * <p>
     * Only the dispatcher is allowed to perform updates.
     * Status transitions are validated against the domain state machine and
     * require an explicit timestamp.
     * </p>
     *
     * @param shipmentId identifier of the shipment
     * @param dto        update payload containing changed fields
     * @param userId     identifier of the requesting user
     * @return updated shipment projection reflecting the current state
     *
     * @throws UnauthorizedException if the user is not allowed to update the shipment
     * @throws InvalidShipmentStatusTransitionException if the status change is not permitted
     */
    @Transactional
    public ShipmentListItemDto update(Long shipmentId, ShipmentUpdateDto dto, Long userId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        if (!shipment.isDispatchedBy(userId)) {
            throw new UnauthorizedException("Only dispatcher can update shipment");
        }

        boolean statusChanged = applyStatusTransition(dto, shipment);
        boolean transportOrderChanged = applyTransportOrder(dto, shipment, LocalDate.now());
        boolean carrierChanged = applyCarrier(dto, shipment);
        applyLicense(dto, shipment);
        applyComments(dto, shipment);
        validateTransportOrderConsistency(shipment);

        if (statusChanged || transportOrderChanged) {
            shipmentNotificationService.publishToParticipants(ShipmentEvent.EventType.STATUS_CHANGED, shipment);
        } else if (carrierChanged) {
            shipmentNotificationService.publishToParticipants(ShipmentEvent.EventType.UPDATED, shipment);
        }

        return assembler.assembleCurrent(shipment);
    }

    /**
     * Applies a validated shipment status transition.
     *
     * <p>
     * This method enforces the shipment state machine rules and ensures that
     * required timestamps are provided for execution-related transitions.
     * </p>
     *
     * @param dto      update request containing the target status
     * @param shipment shipment entity to be modified
     *
     * @throws InvalidShipmentStatusTransitionException if the transition is not allowed
     * @throws DeliveryTimeLineException if the transition timestamp is missing
     */
    private boolean applyStatusTransition(ShipmentUpdateDto dto, Shipment shipment) {
        if (dto.status() == null) return false;
        ShipmentStatus target = ShipmentStatus.valueOf(dto.status().name());

        if (dto.statusAt() == null) {
            throw new DeliveryTimeLineException("Status time must be provided when changing shipment status");
        }

        switch (target) {
            case LOADED -> shipment.markLoaded(dto.statusAt());
            case DROPPED -> shipment.markDropped(dto.statusAt());
            default -> throw new InvalidShipmentStatusTransitionException (shipment.getStatus(), target);
        }

        return true;
    }

    private void applyComments(ShipmentUpdateDto dto, Shipment shipment) {
        if (dto.shipmentComments() != null) {
            if (dto.shipmentComments().isBlank()) {
                shipment.setComments(null);
            } else {
                shipment.setComments(dto.shipmentComments().trim());
            }
        }
    }

    private boolean applyTransportOrder(ShipmentUpdateDto dto, Shipment shipment, LocalDate today) {
        if (dto.transportOrder() == null) return false;

        if (dto.transportOrder().isBlank()) {
            shipment.setTransportOrder(null);
            shipment.setLicensePlate(null);
            if (shipment.getCarrier() != null) shipment.unassignCarrier();

            ShipmentStatus current = shipment.getStatus();
            if (current == ShipmentStatus.PLANNED || current == ShipmentStatus.TO_LOAD) {
                shipment.revertToNew();
                return true;
            }
            return false;
        }

        shipment.setTransportOrder(dto.transportOrder().trim());
        if (shipment.getStatus() == ShipmentStatus.NEW) {
            if (!today.isBefore(shipment.getPlannedLoadDate())) {
                shipment.markToLoadByUser();
            } else {
                shipment.markPlanned(Instant.now());
            }
            return true;
        }

        return false;
    }

    private void applyLicense(ShipmentUpdateDto dto, Shipment shipment) {
        if (dto.licensePlate() != null) {
            if (dto.licensePlate().isBlank()) {
                shipment.setLicensePlate(null);
            } else {
                shipment.setLicensePlate(dto.licensePlate().trim());
            }
        }
    }

    private boolean applyCarrier(ShipmentUpdateDto dto, Shipment shipment) {
        if (dto.carrierName() == null) return false;

        Company currentCarrier = shipment.getCarrier();

        if (dto.carrierName().isBlank()) {
            if (currentCarrier == null) return false;
            shipment.unassignCarrier();
            return true;
        }

        Company newCarrier = companyService.resolveCompany(dto.carrierName());
        if (currentCarrier != null && currentCarrier.getId().equals(newCarrier.getId())) {
            return false;
        }

        shipment.assignCarrier(newCarrier);
        return true;
    }

    private void validateTransportOrderConsistency(Shipment shipment) {
        if (shipment.getTransportOrder() == null) return;

        ValidationResult result = new ValidationResult();

        if (shipment.getCarrier() == null) {
            result.add("carrier", "Carrier is required when transport order is set");
        }
        if (shipment.getLicensePlate() == null || shipment.getLicensePlate().isBlank()) {
            result.add("licensePlate", "License plate is required when transport order is set");
        }

        if (!result.isValid()) {
            throw new ValidationException(result);
        }
    }

    /**
     * Cancels a shipment with the specified reason.
     *
     * <p>
     * Cancellation is only permitted for non-final shipment states and
     * only by the shipment owner.
     * </p>
     *
     * @param shipmentId identifier of the shipment
     * @param reason     cancellation reason
     * @param userId     identifier of the requesting user
     *
     * @throws ShipmentCancellationNotAllowedException if cancellation is not permitted
     */
    @Transactional
    public void cancel(Long shipmentId, ShipmentCancelReason reason, Long userId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        if (!shipment.isOwnedBy(userId)) {
            throw new ShipmentCancellationNotAllowedException(shipmentId);
        }

        shipment.cancel(reason);
        shipmentNotificationService.publishToParticipants(ShipmentEvent.EventType.STATUS_CHANGED, shipment);
    }

}