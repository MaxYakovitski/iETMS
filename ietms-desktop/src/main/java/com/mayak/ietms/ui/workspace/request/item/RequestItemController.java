package com.mayak.ietms.ui.workspace.request.item;

import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.error.UiError;
import com.mayak.ietms.integration.api.RequestClient;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.refuse.RefuseReasonOptionDto;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.ui.core.SecuredView;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.ui.workspace.request.base.RequestsParent;
import com.mayak.ietms.ui.workspace.request.item.bid.AddBidController;
import com.mayak.ietms.ui.workspace.request.item.bid.BidHistoryController;
import com.mayak.ietms.support.enums.View;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.common.TextUtils;
import com.mayak.ietms.infrastructure.fx.VisibilityUtils;
import com.mayak.ietms.infrastructure.status.StatusColorMapper;
import com.mayak.ietms.infrastructure.ui.ItemStyleUtils;
import com.mayak.ietms.infrastructure.ui.ItemVisibilityUtils;
import com.mayak.ietms.infrastructure.ui.LabelTooltipUtils;
import com.mayak.ietms.infrastructure.ui.LocationUIHelper;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.ui.workspace.request.item.bid.FinalPriceController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

@Controller
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class RequestItemController implements ViewLifecycle, SecuredView {

    @FXML public Label customerReference, customer, rIDLabel, tIdLabel, dataStart, dataEnd, requestTypeLabel, shipmentType, transportType,
            dangerousCheck, temperature, weight, loadingMeters, status, customerPriceLabel, authorFullName, requestDateTime;
    @FXML public TextField rId, tId;
    @Getter
    @FXML public HBox requestPane;
    @FXML public Button commentsButton, joinButton, bidButton, priceButton, confirmedAndOfferedButton, acceptButton,
            goToExchangeButton, refuseButton, renewButton, deleteButton, moreButton;
    @FXML public VBox fromISOContainer, toISOContainer, fromPointContainer, toPointContainer;
    @FXML public ImageView joinImageView;

    private static final String JOIN_ICON = "/icons/user_join.png";
    private static final String UNJOIN_ICON = "/icons/user-leave.png";
    private static final String COMMENTS_ICON = "/icons/comments.png";
    private static final String MORE_ICON = "/icons/more.png";

    @Setter
    private Stage stage;

    @Getter @Setter
    private Long requestId;
    private RequestDetailsDto dto;
    private UserResponseDto loggedInUser;
    private RequestsParent parent;
    private boolean initialized = false;
    private  RequestItemActions actions;

    private final RequestClient requestClient;
    private final WindowService windowService;

    @FXML
    void initialize() {
        LabelTooltipUtils.attachAutoTooltip(customerReference, customer, authorFullName);
        priceButton.setOnAction(event -> showBidHistory());
    }

    public void bindRequest(Long requestId) {
        if (Objects.equals(this.requestId, requestId)) return;
        this.requestId = requestId;
        this.dto = null;
        this.initialized = false;
    }

    public void renderSkeleton() {
        customerReference.setText("");
        customer.setText("");
        status.setText("");
        joinButton.setVisible(false);
        confirmedAndOfferedButton.setVisible(false);
        acceptButton.setVisible(false);
        refuseButton.setVisible(false);
        bidButton.setDisable(true);
        priceButton.setDisable(true);
    }

    public void attachDetails(RequestDetailsDto dto) {
        boolean firstBind = !initialized;

        this.dto = dto;
        this.initialized = true;

        if (firstBind) initStaticView();
        updateDynamicView();
    }

    //========= RENDER =======
    public void initStaticView() {
        if (requestId == null || dto == null) return;

        LocationUIHelper.addLocationLabels(dto.fromLocations(), fromISOContainer, fromPointContainer);
        LocationUIHelper.addLocationLabels(dto.toLocations(), toISOContainer, toPointContainer);

        customerReference.setText(Optional.ofNullable(dto.customerReference()).orElse(""));

        customer.setText(Optional.ofNullable(dto.customer()).map(CompanyDto::name).map(String::toUpperCase).orElse(""));

        dataStart.setText(dto.startDate().toLocalDate().format(TextUtils.DATE_FORMATTER));
        dataEnd.setText(dto.endDate().toLocalDate().format(TextUtils.DATE_FORMATTER));

        shipmentType.setText(dto.shipmentType().name());
        transportType.setText(dto.transportType().name());

        temperature.setText(dto.temperature() != null ? TextUtils.formatTemperature(dto.temperature()) : "");
        weight.setText(TextUtils.formatWeight(dto.weight()));
        loadingMeters.setText(TextUtils.formatLoadingMeters(dto.loadingMeter()));
        dangerousCheck.setText(dto.dangerous() ? "ADR" : "");

        commentsButton.setVisible(dto.comments() != null && !dto.comments().isBlank());

        rId.setText(String.valueOf(dto.id()));

        requestTypeLabel.setText(dto.requestType().name());

        authorFullName.setText(dto.author().fullName());

        var localDateTime = dto.issueDate().atZone(ZoneId.systemDefault()).toLocalDateTime();
        requestDateTime.setText(localDateTime.format(TextUtils.DATE_TIME_FORMATTER));

        ItemStyleUtils.applyDefaultTextColor(
                customerReference, customer, dataStart,
                dataEnd, shipmentType, transportType,
                temperature, weight, loadingMeters,
                rIDLabel, tIdLabel, customerPriceLabel,
                requestTypeLabel, authorFullName, requestDateTime
        );

        status.setTextFill(StatusColorMapper.toColor(dto.status()));

        ItemVisibilityUtils.applyStandardRules(
                customerReference, customer, dangerousCheck,
                temperature, weight, loadingMeters, rIDLabel,
                rId, tIdLabel, tId, requestTypeLabel, customerPriceLabel
        );
    }

    public void updateDynamicView() {
        if (dto == null || loggedInUser == null) return;

        tId.setText(dto.tid() != null ? dto.tid() : "");
        status.setText(dto.status() != null ? dto.status().name() : "");
        updateJoinIcon();

        bidButton.setDisable(!RequestItemUiPolicy.canBid(dto));
        bidButton.getStyleClass().setAll("button", RequestItemUiPolicy.canBid(dto) ? "bid-active" : "bid-inactive");

        confirmedAndOfferedButton.setVisible(RequestItemUiPolicy.canConfirm(dto, loggedInUser));
        acceptButton.setVisible(RequestItemUiPolicy.canAccept(dto, loggedInUser));
        refuseButton.setVisible(RequestItemUiPolicy.canRefuse(dto, loggedInUser));

        if (dto.status() == RequestStatusDto.ACCEPTED || dto.status() == RequestStatusDto.REFUSED) {
            status.setTextFill(StatusColorMapper.toColor(dto.status()));
        }

        renewButton.setVisible(RequestItemUiPolicy.canEdit(dto, loggedInUser));
        deleteButton.setVisible(RequestItemUiPolicy.canDelete(dto, loggedInUser));
        refreshPriceButton();

        customerPriceLabel.setText(TextUtils.formatMoney(dto.price()));

        HBox rIdRow = (HBox) rIDLabel.getParent();
        VisibilityUtils.hideContainerIfChildrenEmpty(rIdRow, rId);
        HBox tIdRow = (HBox) tIdLabel.getParent();
        VisibilityUtils.hideContainerIfChildrenEmpty(tIdRow, tId);
        VisibilityUtils.hideIfEmpty(requestTypeLabel, customerPriceLabel);
    }

    //========= EVENT HANDLERS =======
    @FXML
    public void handleJoin() {
        if (requestId == null || dto == null) return;

        if (dto.isJoined()) {
            actions.leave(requestId);
        } else {
            if (!dto.canJoin()) return;
            actions.join(requestId);
        }

    }

    @FXML
    public void handleBid() {
        if (requestId == null || dto == null || !dto.canBid()) return;
        String fxmlPath = View.ADD_BID.getPath();
        windowService.openModalWindow(
                fxmlPath,
                AddBidController.class,
                controller -> {
                    controller.init(requestId);
                    controller.setOnSubmit( bid -> parent.invalidateRequest(requestId));
                },
                "Bid",
                null,
                this.stage
        );
    }

    @FXML
    public void handleConfirm() {
        if (requestId == null || dto == null) return;
        actions.offer(requestId);
    }

    @FXML
    public void handleAccept() {
        if (dto.requestType() == RequestTypeDto.SPOT) {
            windowService.openModalWindow(
                    View.FINAL_PRICE.getPath(),
                    FinalPriceController.class,
                    c -> c.setOnSubmit(price -> actions.acceptWithPrice(requestId, price)),
                    "Final price",
                    null,
                    stage
            );
        } else {
            actions.accept(requestId);
        }
    }

    @FXML
    public void handleRefuse() {
        if (requestId == null || dto == null) return;

        String fxmlPath = View.REFUSE_REASON.getPath();
        windowService.openModalWindow(
                fxmlPath,
                RefuseReasonController.class,
                controller -> {

                    @SuppressWarnings("unchecked")
                    RefuseReasonController<RefuseReasonOptionDto> c =
                            (RefuseReasonController<RefuseReasonOptionDto>) controller;

                    c.init(
                            dto.refuseReasons(),
                            RefuseReasonOptionDto::label,
                            reason -> {
                                actions.refuse(requestId, reason.code());
                                parent.invalidateRequest(requestId);
                            }
                    );
                },
                "Refuse",
                null,
                this.stage
        );
    }

    @FXML
    public void handleComments() {
        if (requestId == null || dto == null || dto.comments() == null) return;

        String fxmlPath = View.REQUEST_COMMENT.getPath();
        windowService.openModalWindow(
                fxmlPath,
                CommentController.class,
                controller -> controller.setCommentsText(dto.comments()),
                "Commentaries",
                COMMENTS_ICON, this.stage);
    }

    @FXML
    public void handleToExchange() {
        if (requestId == null) return;

        String exchangeText = actions.getExchangeString(requestId);

        if (exchangeText == null || exchangeText.isBlank()) {
            AlertUtils.showWarning("Nothing to copy!", stage);
            return;
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(exchangeText);
        Clipboard.getSystemClipboard().setContent(content);

        AlertUtils.showInfo("Request details copied to buffer!", stage);
    }

    @FXML
    public void handleMore() {
        if (requestId == null || dto == null) return;

        String fxmlPath = View.REQUEST_MORE.getPath();

        windowService.openModalWindow(
                fxmlPath,
                RequestMoreController.class,
                controller -> {
                    controller.setRequest(dto);
                    controller.setOnTidUpdated(() -> parent.invalidateRequest(requestId));
                },
                "More",
                MORE_ICON,
                this.stage
        );
    }

    @FXML
    public void handleRenew() {
        if (requestId == null || dto == null) return;

        boolean confirmed = AlertUtils.showConfirmation(
                null,
                "Do you want to edit this request? Your old request will be deleted.");
        if (!confirmed) return;
        parent.fillFormWithRequest(dto);

        try {
            actions.delete(requestId);
            parent.getRequestItems().removeIf(it -> it.id().equals(requestId));
            requestId = null;
            dto = null;

        } catch (Exception e) {
            AlertUtils.showWarning("Request not found or already deleted.");
        }
    }

    @FXML
    public void handleDelete() {
        if (requestId == null) {
            AlertUtils.showInfo("Request is already deleted.");
            return;
        }

        boolean confirmed = AlertUtils.showConfirmation(
                null,
                "Are you sure you want to delete this request? This action cannot be undone.");
        if (!confirmed) return;

        try {
            actions.delete(requestId);

            if (parent != null) {
                parent.getRequestItems()
                        .removeIf(it -> it != null && it.id() != null && it.id().equals(requestId));
            }

            requestId = null;
            dto = null;

        } catch (ApiException ex) {
            UiError error = ApiErrorUtils.resolve(ex, "Failed to delete request.");
            AlertUtils.show(error);
            log.warn("Delete request failed", ex);
        }
    }

    @FXML
    private void showBidHistory() {
        if (requestId == null) return;

        windowService.openModalWindow(
                View.BID_HISTORY.getPath(),
                BidHistoryController.class,
                controller -> {
                    controller.setRequestId(requestId);
                    controller.setLoggedInUser(loggedInUser);
                    controller.setOnChanged(() -> parent.invalidateRequest(requestId));
                    controller.onShow();
                },
                "Bid history",
                null,
                this.stage
        );
    }

    // ==================== CONTEXT ====================
    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.loggedInUser = user;
    }

    public void setContext(UserResponseDto loggedInUser, RequestsParent parent) {
        this.loggedInUser = loggedInUser;
        this.parent = parent;
        this.actions = new RequestItemActions(requestClient, () -> parent.invalidateRequest(requestId));
    }

    public boolean isBlank() {
        return dto == null;
    }

    // ==================== UI HELPERS ====================
    private void refreshPriceButton() {
        if (dto == null || dto.activeBids() == null || dto.activeBids().isEmpty()) {
            priceButton.setText("");
            priceButton.setDisable(true);
            return;
        }

        dto.activeBids().stream()
                .min(Comparator.comparing(BidViewDto::amount).thenComparing(BidViewDto::time))
                .ifPresentOrElse(bid -> {
                    priceButton.setText(bid.amount().toPlainString() + " EUR");
                    boolean hasComment = bid.comment() != null && !bid.comment().isBlank();
                    priceButton.setTextFill(
                            hasComment
                                    ? TextUtils.SYSTEM_TEXT_RED_COLOR
                                    : TextUtils.SYSTEM_TEXT_DEFAULT_COLOR);
                    priceButton.setDisable(false);
                }, () -> {
                    priceButton.setText("");
                    priceButton.setDisable(true);
                });
    }

    private void updateJoinIcon() {
        boolean visible = RequestItemUiPolicy.canShowJoinButton(dto, loggedInUser);
        joinButton.setVisible(visible);
        if (!visible) return;

        boolean joined = dto.isJoined();
        joinImageView.setImage(
                new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream(
                                joined ? UNJOIN_ICON : JOIN_ICON)))
        );

        if (joined) {
            joinImageView.setOpacity(0.3);
            joinButton.setOnMouseEntered(e -> joinImageView.setOpacity(0.65));
            joinButton.setOnMouseExited(e -> joinImageView.setOpacity(0.3));
        } else {
            joinImageView.setOpacity(0.65);
            joinButton.setOnMouseEntered(null);
            joinButton.setOnMouseExited(null);
        }
    }
}