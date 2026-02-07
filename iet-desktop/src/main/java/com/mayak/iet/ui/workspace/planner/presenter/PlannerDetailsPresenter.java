package com.mayak.iet.ui.workspace.planner.presenter;

import com.mayak.iet.domain.planner.model.ShipmentContext;
import com.mayak.iet.domain.planner.policy.ShipmentExecutionPolicy;
import com.mayak.iet.domain.planner.policy.ShipmentPlannerPolicy;
import com.mayak.iet.domain.planner.timeline.ShipmentTimelineService;
import com.mayak.iet.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.ui.workspace.planner.policy.PlannerStatusEditPolicy;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class PlannerDetailsPresenter {

    private final ShipmentExecutionPolicy executionPolicy;
    private final ShipmentTimelineService timelineService;
    private final TimelinePresenter timelinePresenter;
    private final ShipmentPlannerPolicy plannerPolicy;
    private final PlannerStatusEditPolicy statusEditPolicy;


    public void hideAll(VBox shipmentDetails, VBox transportDetails, VBox timelineContainer, Button cancelButton) {
        shipmentDetails.setVisible(false);
        shipmentDetails.setManaged(false);

        transportDetails.setVisible(false);
        transportDetails.setManaged(false);

        timelineContainer.getChildren().clear();
        timelineContainer.setVisible(false);
        timelineContainer.setManaged(false);

        cancelButton.setVisible(false);
        cancelButton.setManaged(false);
    }

    public void showShipmentDetails( ShipmentListItemDto dto,
                                      Label shipmentNumber,
                                      Label dispatcher,
                                      VBox shipmentDetails,
                                      VBox transportDetails,
                                      VBox timelineContainer,
                                      Button cancelButton) {

        transportDetails.setVisible(false);
        transportDetails.setManaged(false);

        shipmentDetails.setVisible(true);
        shipmentDetails.setManaged(true);

        shipmentNumber.setText(String.valueOf(dto.id()));
        dispatcher.setText(dto.dispatcher() != null ? dto.dispatcher().fullName() : "-");
        timelineContainer.getChildren().clear();

        boolean showTimeline = executionPolicy.showTimeline(dto);
        boolean canCancel = executionPolicy.canCancel(dto);

        timelineContainer.setVisible(showTimeline);
        timelineContainer.setManaged(showTimeline);

        cancelButton.setVisible(canCancel);
        cancelButton.setManaged(canCancel);


        if (showTimeline) {
            var entries = timelineService.build(dto.timestamps());
            timelinePresenter.render(timelineContainer, entries);
        }
    }

    public void showTransportDetails(ShipmentListItemDto dto,
                                      ShipmentContext ctx,
                                      Label shipmentNumber,
                                      VBox shipmentDetails,
                                      VBox transportDetails,
                                      TextField carrierField,
                                      TextArea commentsTextArea,
                                      TextField licensePlateField,
                                      TextField transportOrder,
                                      ComboBox<ShipmentStatusDto> statusComboBox,
                                      DatePicker datePicker,
                                      Spinner<LocalTime> timeSpinner) {

        shipmentDetails.setVisible(false);
        shipmentDetails.setManaged(false);

        transportDetails.setVisible(true);
        transportDetails.setManaged(true);
        shipmentNumber.setText(String.valueOf(dto.id()));

        carrierField.setText(dto.carrierName());
        commentsTextArea.setText(dto.shipmentComments());
        licensePlateField.setText(dto.licensePlate());
        transportOrder.setText(dto.transportOrder());

        boolean fieldsEditable = plannerPolicy.canEditTransportFields(ctx);

        carrierField.setDisable(!fieldsEditable);
        licensePlateField.setDisable(!fieldsEditable);
        transportOrder.setDisable(!fieldsEditable);

        commentsTextArea.setDisable(false);

        ShipmentStatusDto allowedStatus = fieldsEditable ? plannerPolicy.allowedNextStatus(ctx).orElse(null) : null;

        statusComboBox.setValue(null);
        statusEditPolicy.reset(datePicker, timeSpinner);
        statusEditPolicy.applyAllowedStatus(statusComboBox, allowedStatus);
    }
}
