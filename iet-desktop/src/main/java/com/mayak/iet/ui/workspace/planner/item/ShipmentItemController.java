package com.mayak.iet.ui.workspace.planner.item;

import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.ui.core.ViewLifecycle;
import com.mayak.iet.ui.workspace.planner.item.event.ShipmentLastEvent;
import com.mayak.iet.ui.workspace.planner.item.event.ShipmentLastEventResolver;
import com.mayak.iet.ui.workspace.planner.item.presenter.ShipmentItemPresenter;
import com.mayak.iet.ui.workspace.planner.item.presenter.ShipmentItemViewData;
import com.mayak.iet.ui.workspace.request.item.CommentController;
import com.mayak.iet.infrastructure.common.TextUtils;
import com.mayak.iet.infrastructure.fx.VisibilityUtils;
import com.mayak.iet.infrastructure.ui.ItemStyleUtils;
import com.mayak.iet.infrastructure.ui.ItemVisibilityUtils;
import com.mayak.iet.infrastructure.ui.LabelTooltipUtils;
import com.mayak.iet.infrastructure.ui.LocationUIHelper;
import com.mayak.iet.infrastructure.window.WindowService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;


@Controller
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class ShipmentItemController implements ViewLifecycle {

    @FXML public HBox requestPane;
    @FXML public VBox fromISOContainer, toISOContainer, fromPointContainer, toPointContainer;
    @FXML public Label customerReference, customer, dataStart, dataEnd, shipmentType, transportType, dangerousCheck,
            temperature, weight, loadingMeters, rIDLabel, tIdLabel, requestTypeLabel, customerPriceLabel,
            carrierLabel, carrierPriceLabel, authorName, authorSurname, eventLabel, eventTime;
    @FXML public Button commentsButton;
    @FXML public TextField rId, tId;

    private static final String COMMENTS_ICON = "/icons/comments.png";

    private final WindowService windowService;
    private final ShipmentItemPresenter presenter;
    private final ShipmentLastEventResolver lastEventResolver;

    @Setter
    private Stage stage;

    @Getter @Setter
    private Long shipmentId;

    private ShipmentListItemDto dto;
    private boolean initialized;

    @FXML
    private void initialize() {
        LabelTooltipUtils.attachAutoTooltip(customerReference, customer, carrierLabel, authorName);
    }

    public void onShowOnce() {
        if (initialized) return;
        initialized = true;

        applyStyles();
    }

    public void updateItem(ShipmentListItemDto dto) {
        this.dto = dto;
        this.shipmentId = dto != null ? dto.id() : null;

        if (dto == null) {
            hideEvent();
            return;
        }

        ShipmentItemViewData v = presenter.present(dto);

        applyTexts(v);
        applyVisibility();
        applyLastEvent();
        updateDynamicView();
    }

    private void applyTexts(ShipmentItemViewData v) {
        rId.setText(v.rId());
        tId.setText(v.tId());

        LocationUIHelper.addLocationLabels(dto.fromLocations(), fromISOContainer, fromPointContainer);
        LocationUIHelper.addLocationLabels(dto.toLocations(), toISOContainer, toPointContainer);

        customerReference.setText(v.customerReference());
        customer.setText(v.customer());

        dataStart.setText(v.dataStart());
        dataEnd.setText(v.dataEnd());

        shipmentType.setText(v.shipmentType());
        transportType.setText(v.transportType());
        requestTypeLabel.setText(v.requestType());

        dangerousCheck.setText(v.dangerous());
        temperature.setText(v.temperature());
        weight.setText(v.weight());
        loadingMeters.setText(v.loadingMeters());

        customerPriceLabel.setText(v.customerPrice());
        carrierPriceLabel.setText(v.carrierPrice());

        authorName.setText(v.authorName());
        authorSurname.setText(v.authorSurname());
        carrierLabel.setText(v.carrier());

        commentsButton.setVisible(v.hasComments());
    }

    private void applyStyles() {
        ItemStyleUtils.applyDefaultTextColor(
                customerReference, customer, dataStart, dataEnd,
                shipmentType, transportType,
                temperature, weight, loadingMeters,
                rIDLabel, tIdLabel, requestTypeLabel,
                customerPriceLabel, carrierLabel, carrierPriceLabel,
                authorName, authorSurname
        );
    }

    private void applyVisibility() {
        ItemVisibilityUtils.applyStandardRules(
                customerReference, customer,
                dangerousCheck,
                temperature, weight, loadingMeters,
                rIDLabel, rId,
                tIdLabel, tId,
                carrierLabel, carrierPriceLabel
        );
    }

    private void applyLastEvent() {
        if (dto == null) {
            hideEvent();
            return;
        }

        lastEventResolver.resolve(dto).ifPresentOrElse(this::showEvent, this::showEmptyEvent);
    }

    private void showEvent(ShipmentLastEvent event) {
        eventLabel.setText(event.label());
        eventLabel.setTextFill(event.color());

        eventTime.setText(event.at() != null ? event.at().format(TextUtils.DATE_TIME_FORMATTER) : "");
        eventTime.setTextFill(event.color());

        eventLabel.setVisible(true);
        eventLabel.setManaged(true);
        eventTime.setVisible(true);
        eventTime.setManaged(true);
    }

    private void showEmptyEvent() {
        eventLabel.setText("—");
        eventLabel.setTextFill(TextUtils.SYSTEM_TEXT_DEFAULT_COLOR);

        eventLabel.setVisible(true);
        eventLabel.setManaged(true);
        eventTime.setVisible(false);
        eventTime.setManaged(false);
    }

    private void hideEvent() {
        eventLabel.setText("");
        eventTime.setText("");

        eventLabel.setVisible(false);
        eventLabel.setManaged(false);
        eventTime.setVisible(false);
        eventTime.setManaged(false);
    }

    private void updateDynamicView() {
        if (dto == null) return;
        HBox rIdRow = (HBox) rIDLabel.getParent();
        VisibilityUtils.hideContainerIfChildrenEmpty(rIdRow, rId);
        HBox tIdRow = (HBox) tIdLabel.getParent();
        VisibilityUtils.hideContainerIfChildrenEmpty(tIdRow, tId);
    }

    @FXML
    public void handleComments() {
        if (dto == null || dto.id() == null) {
            return;
        }

        String comments = TextUtils.safeTrim(dto.requestComments());
        if (comments.isEmpty()) return;

        windowService.openModalWindow(
                View.REQUEST_COMMENT.getPath(),
                CommentController.class,
                controller -> controller.setCommentsText(comments),
                "Commentaries",
                COMMENTS_ICON,
                this.stage
        );
    }

}