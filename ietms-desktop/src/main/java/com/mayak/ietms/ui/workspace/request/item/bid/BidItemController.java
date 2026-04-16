package com.mayak.ietms.ui.workspace.request.item.bid;

import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.integration.api.BidClient;
import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.ui.core.BidItemHost;
import com.mayak.ietms.ui.core.SecuredView;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.infrastructure.common.TextUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.time.ZoneId;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class BidItemController implements ViewLifecycle, SecuredView {

    @FXML
    private HBox root;
    @FXML private Label amountLabel, authorLabel, timeLabel;
    @FXML private TextArea comments;
    @FXML private Button deleteButton;

    private final BidClient bidClient;

    @Setter
    private BidItemHost host;
    @Setter
    private BidViewDto bid;
    @Setter
    private RequestStatusDto requestStatus;

    private UserResponseDto loggedInUser;
    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.loggedInUser = user;
    }

    @Override
    public void onShow() {
        updateView();
    }

    public void updateView() {
        amountLabel.setText(bid.amount().toPlainString() + " EUR");
        authorLabel.setText(bid.user() != null ? bid.user().fullName() : "");

        if (bid.time() != null) {
            var localDateTime = bid.time().atZone(ZoneId.systemDefault()).toLocalDateTime();
            timeLabel.setText(localDateTime.format(TextUtils.DATE_TIME_FORMATTER));
            timeLabel.setVisible(true);
        } else {
            timeLabel.setVisible(false);
        }

        boolean hasComment = bid.comment() != null && !bid.comment().isBlank();
        comments.setVisible(hasComment);
        comments.setManaged(hasComment);
        comments.setText(hasComment ? bid.comment() : "");

        boolean isOwner =
                bid.userId() != null &&
                loggedInUser != null &&
                bid.userId().equals(loggedInUser.id());

        boolean canDelete = isOwner
                        && !bid.deleted()
                        && requestStatus != RequestStatusDto.ACCEPTED
                        && requestStatus != RequestStatusDto.REFUSED;

        deleteButton.setVisible(canDelete);
        deleteButton.setManaged(canDelete);

        if (bid.deleted()) {
            root.setOpacity(0.5);
        }
    }

    @FXML
    private void handleDelete() {
        try {
            bidClient.delete(bid.id());
            if (host != null) {
                host.onBidDeleted(bid.id());
            }
        } catch (Exception e) {
            log.error("Failed to delete bid {}", bid.id(), e);
        }
    }
}