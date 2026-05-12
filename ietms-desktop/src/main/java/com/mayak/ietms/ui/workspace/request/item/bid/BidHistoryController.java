package com.mayak.ietms.ui.workspace.request.item.bid;

import com.mayak.ietms.integration.api.RequestClient;
import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.ui.core.BidItemHost;
import com.mayak.ietms.ui.core.SecuredView;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.infrastructure.window.WindowService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
@FxmlView("bid_history.fxml")
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class BidHistoryController implements ViewLifecycle, SecuredView, BidItemHost {

    private static final int BID_ITEM_HEIGHT = 50;
    private static final int MAX_VISIBLE_BIDS_WITHOUT_SCROLL = 10;

    @FXML
    public ScrollPane scrollPane;

    @FXML
    public VBox bidsContainerPlain, bidsContainerScroll;

    @FXML
    public Button archivedButton, closeButton;

    @FXML
    public Label emptyMessageLabel;

    private final WindowService windowService;
    private final FxWeaver fxWeaver;
    private final RequestClient requestClient;

    @Setter
    private Stage stage;

    @Setter
    private Long requestId;

    @Setter
    private Runnable onChanged;

    private boolean showArchived = false;
    private UserResponseDto loggedInUser;

    @Override
    public void setLoggedInUser(UserResponseDto loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    @Override
    public void onShow() {
        reloadBids();
    }

    private void reloadBids() {
        if (requestId == null) {
            showEmpty(true);
            hideContainers();
            return;
        }

        RequestDetailsDto request = requestClient.getDetails(requestId);
        List<BidViewDto> bids = request.bids().stream()
                .filter(bid -> showArchived || !bid.deleted())
                .sorted(Comparator.comparing(BidViewDto::time).reversed())
                .toList();

        if (bids.isEmpty()) {
            showEmpty(true);
            hideContainers();
            return;
        }

        showEmpty(false);
        buildView(bids, request.status());
    }


    private void buildView(List<BidViewDto> bids, RequestStatusDto status) {
        boolean useScroll = bids.size() > MAX_VISIBLE_BIDS_WITHOUT_SCROLL;

        VBox target = useScroll ? bidsContainerScroll : bidsContainerPlain;
        target.getChildren().clear();

        for (BidViewDto bid : bids) {
            Parent node = createBidNode(bid, status);
            if (node != null) {
                target.getChildren().add(node);
            }
        }

        bidsContainerPlain.setVisible(!useScroll);
        bidsContainerPlain.setManaged(!useScroll);

        scrollPane.setVisible(useScroll);
        scrollPane.setManaged(useScroll);

        if (useScroll) {
            int visibleCount = Math.min(bids.size(), MAX_VISIBLE_BIDS_WITHOUT_SCROLL);
            scrollPane.setPrefHeight(BID_ITEM_HEIGHT * visibleCount);
        }

        if (stage != null) {
            stage.sizeToScene();
            windowService.centerOnScreen((Stage) stage.getOwner(), stage);
            windowService.fadeIn(stage, 180);
        }
    }

    private Parent createBidNode(BidViewDto bid, RequestStatusDto status) {
        try {
            var loaded = fxWeaver.load(BidItemController.class);
            BidItemController controller = loaded.getController();
            controller.setBid(bid);
            controller.setRequestStatus(status);
            controller.setLoggedInUser(loggedInUser);
            controller.setHost(this);
            controller.onShow();

            Parent node = (Parent) loaded.getView().orElseThrow();
            node.getStyleClass().add("bid-item");

            return node;
        } catch (Exception e) {
            log.error("Failed to load BidItem for bid {}", bid.id(), e);
            return null;
        }
    }

    private void showEmpty(boolean value) {
        emptyMessageLabel.setVisible(value);
        emptyMessageLabel.setManaged(value);
    }

    private void hideContainers() {
        bidsContainerPlain.setVisible(false);
        bidsContainerPlain.setManaged(false);
        scrollPane.setVisible(false);
        scrollPane.setManaged(false);
    }

    @FXML
    private void handleArchived() {
        showArchived = !showArchived;
        reloadBids();
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }

    @Override
    public void onBidDeleted(long bidId) {
        reloadBids();
        if (onChanged != null) {
            onChanged.run();
        }
    }
}