package com.mayak.ietms.ui.workspace.request.item;

import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.user.dto.UserLookupDto;
import com.mayak.ietms.integration.api.RequestClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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
    @FXML public VBox tidContainer;
    @FXML public TextField tId;

    private final RequestClient requestClient;

    @Setter
    private RequestDetailsDto details;
    @Setter
    private Runnable onTidUpdated;

    public void setRequest(RequestDetailsDto details) {
        if (details == null) return;

        this.details = details;

        renderParticipants();
        renderDispatcher();
        renderTid();
        setupTidAccess();
    }

    @FXML
    public void handleSave() {
        saveTId();
        tidContainer.requestFocus();
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

        tidContainer.setVisible(canEditTid);
        tidContainer.setManaged(canEditTid);

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