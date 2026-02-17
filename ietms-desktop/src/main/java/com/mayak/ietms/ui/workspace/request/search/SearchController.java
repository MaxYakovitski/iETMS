package com.mayak.ietms.ui.workspace.request.search;

import com.mayak.ietms.ui.workspace.request.base.RequestsParent;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
@Slf4j
public class SearchController {

    @FXML
    public TextField searchField;

    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));

    @Getter private Stage stage;
    private RequestsParent parent;

    @FXML
    private void initialize() {
        debounce.setOnFinished(event -> {
            if (parent == null) return;

            String value = searchField.getText();
            String query = (value == null || value.isBlank()) ? null : value.trim();
            parent.applySearch(query);
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                debounce.playFromStart()
        );
    }

    public void init(RequestsParent parent) {
        this.parent = parent;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(e -> {
            if (parent != null) {
                parent.applySearch(null);
            }
        });
    }
}