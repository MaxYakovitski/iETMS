package com.mayak.iet.ui.workspace.request.item;

import com.mayak.iet.request.dto.view.RequestDetailsDto;
import com.mayak.iet.user.dto.UserLookupDto;
import com.mayak.iet.integration.api.RequestClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.stream.Collectors;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class RequestMoreController {

    @FXML public Label participantsLabel, participantsGroup, dispatchedToLabel, dispatcherFullName, tIdLabel;

    @FXML public HBox tidContainer;
    @FXML public TextField tId;

    private final RequestClient requestClient;

    @Setter
    private RequestDetailsDto details;
    @Setter
    private Runnable onTidUpdated;

    @FXML
    public void initialize() {
        tId.setOnMouseClicked(event -> {
            if (!tId.isEditable() && details != null && details.isAuthor()) {
                enableTidEditing();
            }
        });

        tId.setOnAction(event -> {
            if (tId.isEditable()) {
                saveTId();
            }
        });

        tId.focusedProperty().addListener((foc, oldFoc, newFoc) -> {
            if (!newFoc && tId.isEditable()) {
                saveTId();
            }
        });
    }

    public void setRequest(RequestDetailsDto details) {
        if (details == null) return;

        this.details = details;

        participantsGroup.setText(
                details.competitors() != null && !details.competitors().isEmpty()
                        ? details.competitors().stream()
                        .map(UserLookupDto::fullName)
                        .collect(Collectors.joining(", "))
                        : "nobody joined yet"
        );
        dispatcherFullName.setText(
                details.dispatchedUser() != null
                        ? details.dispatchedUser().fullName()
                        : "not identified yet"
        );
        tId.setText(details.tid() != null ? details.tid() : "");
        tId.setEditable(false);
        tId.getStyleClass().remove("editing");

        setupTidAccess();
    }

    private void setupTidAccess() {
        boolean canEditTid = details.isAuthor();

        tidContainer.setVisible(canEditTid);
        tidContainer.setManaged(canEditTid);

        tId.setFocusTraversable(false);
    }

    private void enableTidEditing() {
        tId.setEditable(true);
        tId.requestFocus();

        if (!tId.getStyleClass().contains("editing")) {
            tId.getStyleClass().add("editing");
        }
    }

    private void saveTId() {
        if (details == null || !details.isAuthor()) return;

        String value = tId.getText();
        if (value == null || value.isBlank()) return;

        requestClient.updateTid(details.id(), tId.getText());

        tId.setEditable(false);
        tId.getStyleClass().remove("editing");

        details = requestClient.getDetails(details.id());

        if (onTidUpdated != null) {
            onTidUpdated.run();
        }

        log.info("Updated tid='{}' for details {}", tId.getText(), details.id());
    }
}