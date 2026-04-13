package com.mayak.ietms.ui.workspace.planner.presenter;

import com.mayak.ietms.domain.planner.policy.ShipmentExecutionPolicy;
import com.mayak.ietms.domain.planner.policy.ShipmentPlannerPolicy;
import com.mayak.ietms.domain.planner.timeline.ShipmentTimelineService;
import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.ui.workspace.planner.policy.PlannerStatusEditPolicy;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlannerDetailsPresenter {

    private final ShipmentExecutionPolicy executionPolicy;
    private final ShipmentTimelineService timelineService;
    private final TimelinePresenter timelinePresenter;
    private final ShipmentPlannerPolicy plannerPolicy;
    private final PlannerStatusEditPolicy statusEditPolicy;


    public void hideAll(VBox shipmentDetails, VBox shipmentTimeline, VBox transportDetails, VBox transportTimeline, Button cancelButton) {
        shipmentDetails.setVisible(false);
        shipmentDetails.setManaged(false);

        shipmentTimeline.getChildren().clear();
        shipmentTimeline.setVisible(false);
        shipmentTimeline.setManaged(false);

        transportDetails.setVisible(false);
        transportDetails.setManaged(false);

        transportTimeline.getChildren().clear();
        transportTimeline.setVisible(false);
        transportTimeline.setManaged(false);

        cancelButton.setVisible(false);
        cancelButton.setManaged(false);
    }

    public void showShipmentDetails(ShipmentListItemDto dto,
                                    Label shipmentNumber,
                                    Label dispatcher,
                                    VBox shipmentDetails,
                                    VBox timelineContainer,
                                    Button cancelButton) {

        shipmentDetails.setVisible(true);
        shipmentDetails.setManaged(true);

        shipmentNumber.setText(String.valueOf(dto.id()));
        dispatcher.setText(dto.dispatcher() != null ? dto.dispatcher().fullName() : "-");

        boolean showTimeline = executionPolicy.showTimeline(dto);
        boolean canCancel = executionPolicy.canCancel(dto);

        timelineContainer.getChildren().clear();
        timelineContainer.setVisible(showTimeline);
        timelineContainer.setManaged(showTimeline);

        cancelButton.setVisible(canCancel);
        cancelButton.setManaged(canCancel);

        if (showTimeline) {
            var entries = timelineService.build(dto.timestamps());
            timelinePresenter.render(timelineContainer, entries);
        }
    }

    public void showTransportDetails(TransportDetailsInput input) {

        input.transportDetails().setVisible(true);
        input.transportDetails().setManaged(true);

        input.shipmentNumber().setText(String.valueOf(input.dto().id()));
        input.carrierField().setText(input.dto().carrierName());
        input.licensePlateField().setText(input.dto().licensePlate());
        input.transportOrder().setText(input.dto().transportOrder());
        input.commentsTextArea().setText(input.dto().shipmentComments());

        boolean fieldsEditable = plannerPolicy.canEditCarrierFields(input.ctx());
        input.carrierField().setDisable(!fieldsEditable);
        input.licensePlateField().setDisable(!fieldsEditable);
        input.transportOrder().setDisable(!fieldsEditable);
        input.commentsTextArea().setDisable(false);

        ShipmentStatusDto allowedStatus = plannerPolicy.allowedNextStatus(input.ctx()).orElse(null);

        input.statusComboBox().getSelectionModel().clearSelection();
        statusEditPolicy.reset(input.datePicker(), input.timeSpinner());
        statusEditPolicy.applyAllowedStatus(input.statusComboBox(), allowedStatus);

        input.timelineContainer().getChildren().clear();
        var entries = timelineService.build(input.dto().timestamps());
        timelinePresenter.render(input.timelineContainer(), entries);
    }
}