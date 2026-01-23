package com.mayak.iet.ui.workspace.request.item.bid;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.function.Consumer;

@Controller
@Scope("prototype")
@Setter
public class FinalPriceController {

    @FXML private TextField priceField;

    private Consumer<BigDecimal> onSubmit;
    private Stage stage;

    @FXML
    public void handleSubmit() {
        clearError();
        try {

            String raw = priceField.getText();
            if (raw == null || raw.isBlank()) {
                markError();
                return;
            }

            BigDecimal price = new BigDecimal(raw.replace(",", "."));
            if (onSubmit != null) {
                onSubmit.accept(price);
            }

            if (stage != null) {
                stage.close();
            }
        } catch (NumberFormatException e) {
            markError();
        }
    }

    @FXML
    public void handleCancel() {
        stage.close();
    }

    private void markError() {
        priceField.getStyleClass().add("price-field-error");
    }

    private void clearError() {
        priceField.getStyleClass().remove("price-field-error");
    }
}