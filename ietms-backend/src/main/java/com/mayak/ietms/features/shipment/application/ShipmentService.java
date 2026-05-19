package com.mayak.ietms.features.shipment.application;

import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.features.shipment.application.notify.ShipmentNotificationService;
import com.mayak.ietms.features.shipment.application.assembly.ShipmentListItemAssembler;
import com.mayak.ietms.shared.exception.business.*;
import com.mayak.ietms.shared.exception.validation.ValidationException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

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
@Slf4j
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
     * Returns all active transports dispatched to the given user.
     *
     * <p>
     * Transport is considered active if its status is not final
     * ({@code DROPPED} or {@code CANCELED}), or if it reached a final state
     * today — allowing the dispatcher to see completed work until end of day.
     * </p>
     *
     * @param userId identifier of the transport specialist
     * @return list of current shipment projections sorted by status
     */
    public List<ShipmentListItemDto> findMyActiveTransports(Long userId) {
        Instant startOfDay = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        return shipmentRepository
                .findMyActiveTransports(userId, List.of(ShipmentStatus.DROPPED, ShipmentStatus.CANCELED), startOfDay)
                .stream()
                .map(assembler::assembleCurrent)
                .toList();
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
     * <p>
     * After applying the requested transition, an automatic promotion to
     * {@code TO_DROP} is applied if the shipment is {@code LOADED} and its
     * planned drop date has arrived.
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
        LocalDate today = LocalDate.now();
        boolean statusChanged = applyStatusTransition(dto, shipment);
        promoteToDropIfNeeded(shipment, today);
        boolean transportOrderChanged = applyTransportOrder(dto, shipment, today);
        boolean carrierChanged = applyCarrier(dto, shipment);
        applyLicense(dto, shipment);
        applyComments(dto, shipment);
        validateTransportOrderConsistency(shipment);
        if (statusChanged || transportOrderChanged) {
            shipmentNotificationService.publishToParticipants(ShipmentEvent.EventType.STATUS_CHANGED, shipment);
        } else if (carrierChanged) {
            shipmentNotificationService.publishToParticipants(ShipmentEvent.EventType.UPDATED, shipment);
        }
        log.info("Shipment updated: shipmentId={}, userId={}", shipmentId, userId);
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
            promoteToLoadIfNeeded(shipment, today);
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

    private void promoteToLoadIfNeeded(Shipment shipment, LocalDate today) {
        if (!today.isBefore(shipment.getPlannedLoadDate())) {
            shipment.markToLoadByUser();
        } else {
            shipment.markPlanned(Instant.now());
        }
    }

    private void promoteToDropIfNeeded(Shipment shipment, LocalDate today) {
        if (shipment.getStatus() == ShipmentStatus.LOADED
                && !today.isBefore(shipment.getPlannedDropDate())) {
            shipment.markToDropByUser();
        }
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
        log.info("Shipment cancelled: shipmentId={}, reason={}, userId={}", shipmentId, reason, userId);
        shipmentNotificationService.publishToParticipants(ShipmentEvent.EventType.STATUS_CHANGED, shipment);
    }
}