package com.mayak.ietms.ui.workspace.request.item;

import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.error.UiError;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.user.dto.UserLookupDto;
import com.mayak.ietms.integration.api.RequestClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.stream.Collectors;

@Controller
@FxmlView("request_item_more.fxml")
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class RequestMoreController {

    @FXML
    public Label participantsLabel, participantsGroup, dispatchedToLabel, dispatcherFullName, tIdLabel;

    @FXML
    public VBox authorContainer, deleteContainer;

    @FXML
    public TextField tId;

    private final RequestClient requestClient;

    @Setter
    private Stage stage;
    @Setter
    private RequestDetailsDto details;
    @Setter
    private Runnable onTidUpdated;
    @Setter
    private Runnable onDeleted;
    @Setter
    private boolean canDelete;

    public void setRequest(RequestDetailsDto details) {
        if (details == null) return;
        this.details = details;
        renderParticipants();
        renderDispatcher();
        renderTid();
        setupTidAccess();
        setupDeleteAccess();
    }

    @FXML
    public void handleSaveTidNumber() {
        saveTId();
        if (stage != null) stage.close();
    }

    @FXML
    public void handleDelete() {
        if (details == null) {
            AlertUtils.showInfo("Request is already deleted.");
            return;
        }

        boolean confirmed = AlertUtils.showConfirmation(
                null,
                "Are you sure you want to delete this request? This action cannot be undone.",
                stage);
        if (!confirmed) return;

        try {
            requestClient.delete(details.id());
            details = null;
            if (stage != null) stage.close();
            if (onDeleted != null) onDeleted.run();

        } catch (ApiException ex) {
            UiError error = ApiErrorUtils.resolve(ex, "Failed to delete request.");
            AlertUtils.show(error, stage);
            log.warn("Delete request failed", ex);
        }
    }

    private void renderParticipants() {
        participantsGroup.setText(
                details.competitors() != null && !details.competitors().isEmpty()
                        ? details.competitors().stream()
                        .map(UserLookupDto::fullName)
                        .collect(Collectors.joining(", "))
                        : "nobody joined yet"
        );
    }

    private void renderDispatcher() {
        dispatcherFullName.setText(
                details.dispatchedUser() != null
                        ? details.dispatchedUser().fullName()
                        : "not identified yet"
        );
    }

    private void renderTid() {
        tId.setText(details.tid() != null ? details.tid() : "");
    }

    private void setupTidAccess() {
        boolean canEditTid = details.isAuthor();
        authorContainer.setVisible(canEditTid);
        authorContainer.setManaged(canEditTid);
    }

    private void setupDeleteAccess() {
        deleteContainer.setVisible(canDelete);
        deleteContainer.setManaged(canDelete);
    }

    private void saveTId() {
        if (details == null || !details.isAuthor()) return;
        requestClient.updateTid(details.id(), tId.getText());
        details = requestClient.getDetails(details.id());
        if (onTidUpdated != null) {
            onTidUpdated.run();
        }
        log.info("Updated tid='{}' for details {}", tId.getText(), details.id());
    }
}