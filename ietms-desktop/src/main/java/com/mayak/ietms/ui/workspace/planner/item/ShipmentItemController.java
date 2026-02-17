package com.mayak.ietms.ui.workspace.planner.item;

import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.support.enums.View;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.ui.workspace.planner.enums.ActiveTab;
import com.mayak.ietms.ui.workspace.planner.item.event.ShipmentStatusResolver;
import com.mayak.ietms.ui.workspace.planner.item.event.ShipmentStatusView;
import com.mayak.ietms.ui.workspace.planner.item.presenter.ShipmentItemPresenter;
import com.mayak.ietms.ui.workspace.planner.item.presenter.ShipmentItemViewData;
import com.mayak.ietms.ui.workspace.request.item.CommentController;
import com.mayak.ietms.infrastructure.common.TextUtils;
import com.mayak.ietms.infrastructure.fx.VisibilityUtils;
import com.mayak.ietms.infrastructure.ui.ItemStyleUtils;
import com.mayak.ietms.infrastructure.ui.ItemVisibilityUtils;
import com.mayak.ietms.infrastructure.ui.LabelTooltipUtils;
import com.mayak.ietms.infrastructure.ui.LocationUIHelper;
import com.mayak.ietms.infrastructure.window.WindowService;
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
            carrierLabel, carrierPriceLabel, authorName, authorSurname, statusLabel;
    @FXML public Button commentsButton;
    @FXML public TextField rId, tId;

    private static final String COMMENTS_ICON = "/icons/comments.png";

    private final WindowService windowService;
    private final ShipmentItemPresenter presenter;
    private final ShipmentStatusResolver statusResolver;

    @Setter
    private Stage stage;

    @Setter
    private ActiveTab activeTab;

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
            return;
        }

        ShipmentItemViewData view = presenter.present(dto);
        applyTexts(view);
        applyVisibility();
        applyStatus();
        updateDynamicView();
    }

    private void applyTexts(ShipmentItemViewData view) {
        rId.setText(view.rId());
        tId.setText(view.tId());

        LocationUIHelper.addLocationLabels(dto.fromLocations(), fromISOContainer, fromPointContainer);
        LocationUIHelper.addLocationLabels(dto.toLocations(), toISOContainer, toPointContainer);

        customerReference.setText(view.customerReference());
        customer.setText(view.customer());

        dataStart.setText(view.dataStart());
        dataEnd.setText(view.dataEnd());

        shipmentType.setText(view.shipmentType());
        transportType.setText(view.transportType());
        requestTypeLabel.setText(view.requestType());

        dangerousCheck.setText(view.dangerous());
        temperature.setText(view.temperature());
        weight.setText(view.weight());
        loadingMeters.setText(view.loadingMeters());

        customerPriceLabel.setText(view.customerPrice());
        carrierPriceLabel.setText(view.carrierPrice());

        authorName.setText(view.authorName());
        authorSurname.setText(view.authorSurname());
        carrierLabel.setText(view.carrier());

        commentsButton.setVisible(view.hasComments());
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

    private void applyStatus() {
        if (dto == null || activeTab == ActiveTab.MY_SHIPMENTS) {
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
            return;
        }

        ShipmentStatusView view = statusResolver.resolve(dto);

        statusLabel.setText(view.label());
        statusLabel.setTextFill(view.color());

        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
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