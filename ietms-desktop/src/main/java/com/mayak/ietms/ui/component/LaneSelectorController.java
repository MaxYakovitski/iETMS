package com.mayak.ietms.ui.component;

import com.mayak.ietms.lane.dto.LaneViewDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import com.mayak.ietms.infrastructure.common.TextUtils;
import com.mayak.ietms.common.util.formatting.LocationTextFormatter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@FxmlView("lanes_selector.fxml")
@Scope("prototype")
public class LaneSelectorController {

    @FXML
    public TableView<LaneViewDto> laneTable;

    @FXML
    public TableColumn<LaneViewDto, String> idColumn, fromColumn, toColumn;

    @FXML
    public TableColumn <LaneViewDto, ShipmentTypeDto> shipmentColumn;

    @FXML
    public TableColumn <LaneViewDto, TransportTypeDto> transportColumn;

    @FXML
    public TableColumn <LaneViewDto, BigDecimal> priceColumn;

    @FXML
    public TableColumn<LaneViewDto, String> validFrom, validTo;

    @FXML
    public Button select, cancel;

    @Setter
    private List<LaneViewDto> lanes;

    @Getter
    private LaneViewDto selectedLane;

    @Setter
    private Stage stage;

    @FXML
    public void initialize() {
        laneTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        idColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().laneName()));
        fromColumn.setCellValueFactory(c ->
                new SimpleStringProperty(LocationTextFormatter.format(c.getValue().fromLocation())));
        toColumn.setCellValueFactory(c ->
                new SimpleStringProperty(LocationTextFormatter.format(c.getValue().toLocation())));
        shipmentColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().shipmentType()));
        transportColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().transportType()));
        priceColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().totalPrice()));
        validFrom.setCellValueFactory(c -> new SimpleStringProperty(
                        c.getValue().validFrom() == null
                                ? ""
                                : TextUtils.DATE_FORMATTER.format(c.getValue().validFrom())
                )
        );

        validTo.setCellValueFactory(c -> new SimpleStringProperty(
                        c.getValue().validTo() == null
                                ? ""
                                : TextUtils.DATE_FORMATTER.format(c.getValue().validTo())
                )
        );

        select.setOnAction(event -> select());
        cancel.setOnAction(event -> handleCancel());

        laneTable.setRowFactory(tv -> {
            TableRow<LaneViewDto> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    selectedLane = row.getItem();
                    stage.close();
                }
            });

            return row;
        });
    }

    @FXML
    public void onShown() {
        if (lanes != null) {
            laneTable.getItems().setAll(lanes);
        }
    }

    @FXML
    private void select() {
        selectedLane = laneTable.getSelectionModel().getSelectedItem();
        stage.close();
    }

    @FXML
    public void handleCancel() {
        selectedLane = null;
        stage.close();
    }
}