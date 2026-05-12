package com.mayak.ietms.ui.workspace.request.item.bid;

import com.mayak.ietms.request.dto.bid.BidCreateDto;
import com.mayak.ietms.integration.api.BidClient;
import com.mayak.ietms.infrastructure.common.ErrorUtils;
import com.mayak.ietms.infrastructure.common.PriceFieldUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.function.Consumer;

@Controller
@FxmlView("request_item_add_bid.fxml")
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class AddBidController {

    @FXML
    private TextField bidInput;

    @FXML
    private TextArea commentsInput;

    private final BidClient bidClient;

    private Long requestId;
    @Setter
    private Stage stage;
    @Setter
    private Consumer<Void> onSubmit;

    @FXML
    public void initialize() {
        PriceFieldUtils.setupPriceField(bidInput);
    }

    public void init(Long requestId) {
        this.requestId = requestId;
    }

    @FXML
    public void handleSubmit() {
        if (!PriceFieldUtils.validate(bidInput)) return;

        BigDecimal amount = PriceFieldUtils.parseOrNull(bidInput);
        if (amount == null) {
            ErrorUtils.addErrorStyle(bidInput);
            return;
        }

        String comment = commentsInput.getText();
        BidCreateDto dto = new BidCreateDto(
                requestId,
                amount,
                comment == null ? "" : comment.trim()
        );

        bidClient.create(dto);

        if (onSubmit != null) onSubmit.accept(null);
        if (stage != null) stage.close();
    }

    @FXML
    public void handleCancel() {
        if (stage != null) stage.close();
    }

}