package com.mayak.ietms.ui.workspace.request.search;

import com.mayak.ietms.ui.workspace.request.base.RequestsParent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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

    @Getter private Stage stage;
    private RequestsParent parent;

    @FXML
    private void initialize() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (parent == null) return;

            String query =
                    (newVal == null || newVal.isBlank())
                            ? null
                            : newVal.trim();

            parent.applySearch(query);
        });
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