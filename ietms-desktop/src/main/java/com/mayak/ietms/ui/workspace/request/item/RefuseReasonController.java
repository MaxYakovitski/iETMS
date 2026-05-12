package com.mayak.ietms.ui.workspace.request.item;

import com.mayak.ietms.infrastructure.fx.MultiSelectComboBoxUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Controller
@FxmlView("request_item_refuse_reason.fxml")
@Scope("prototype")
@Setter
public class RefuseReasonController<T> {

    @FXML
    public ComboBox<T> reasonChoiceBox;

    @FXML
    public Button submitButton, cancelButton;

    private Stage stage;
    private Consumer<T> onSubmit;

    public void init(List<T> reasons, Function<T, String> displayFunction, Consumer<T> onSubmit) {
        this.onSubmit = onSubmit;
        MultiSelectComboBoxUtils.setupSingleSelect(reasonChoiceBox, reasons, displayFunction);
    }

    @FXML
    private void handleSubmit() {
        T selected = reasonChoiceBox.getValue();
        if (selected == null) return;

        onSubmit.accept(selected);

        if (stage != null) stage.close();
    }

    @FXML
    private void handleCancel() {if (stage != null) stage.close();}
}