package com.mayak.iet.ui.workspace.planner.form;

import com.mayak.iet.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.shipment.dto.view.ShipmentUpdateDto;
import com.mayak.iet.infrastructure.common.TextUtils;

import java.time.LocalDateTime;
import java.util.Objects;

public class ShipmentTransportFormState {

    public record Snapshot(
            String carrier,
            String comments,
            String licensePlate,
            String transportOrder,
            ShipmentStatusDto status
    ) {}

    private Snapshot original;
    private Snapshot current;

    public void bindTo(ShipmentListItemDto dto) {
        this.original = snapshotFromDto(dto);
        this.current = this.original;
    }

    public void updateCurrent(
            String carrier,
            String comments,
            String licensePlate,
            String transportOrder,
            ShipmentStatusDto status
    ) {
        this.current = new Snapshot(
                TextUtils.safeTrim(carrier),
                TextUtils.safeTrim(comments),
                TextUtils.safeTrim(licensePlate),
                TextUtils.safeTrim(transportOrder),
                status
        );
    }

    public boolean isDirty() {
        if (original == null || current == null) return false;
        return !Objects.equals(original, current);
    }

    public ShipmentUpdateDto toUpdateDto(Long shipmentId, LocalDateTime statusAt) {
        if (original == null || current == null) return null;

        String carrierToSend =
                Objects.equals(TextUtils.safeTrim(original.carrier()), TextUtils.safeTrim(current.carrier()))
                        ? null
                        : TextUtils.safeTrim(current.carrier());

        String commentsToSend =
                Objects.equals(TextUtils.safeTrim(original.comments()), TextUtils.safeTrim(current.comments()))
                        ? null
                        : TextUtils.safeTrim(current.comments());

        String licenseToSend =
                Objects.equals(TextUtils.safeTrim(original.licensePlate()), TextUtils.safeTrim(current.licensePlate()))
                        ? null
                        : TextUtils.safeTrim(current.licensePlate());

        String orderToSend =
                Objects.equals(TextUtils.safeTrim(original.transportOrder()), TextUtils.safeTrim(current.transportOrder()))
                        ? null
                        : TextUtils.safeTrim(current.transportOrder());

        ShipmentStatusDto statusToSend =
                original.status() == current.status()
                        ? null
                        : current.status();

        if (carrierToSend == null
                && commentsToSend == null
                && licenseToSend == null
                && orderToSend == null
                && statusToSend == null) {
            return null;
        }

        return new ShipmentUpdateDto(
                shipmentId,
                carrierToSend,
                commentsToSend,
                statusToSend,
                licenseToSend,
                orderToSend,
                statusToSend != null ? statusAt : null
        );
    }

    private Snapshot snapshotFromDto(ShipmentListItemDto dto) {
        return new Snapshot(
                TextUtils.safeTrim(dto.carrierName()),
                TextUtils.safeTrim(dto.shipmentComments()),
                TextUtils.safeTrim(dto.licensePlate()),
                TextUtils.safeTrim(dto.transportOrder()),
                dto.status()
        );
    }

    public void reset() {
        original = null;
        current = null;
    }

}
