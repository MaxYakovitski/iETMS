package com.mayak.ietms.infrastructure.window;

import com.mayak.ietms.ui.core.ViewLifecycle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import java.util.List;
import java.util.Objects;

public class PopupMenuUtils {
    private static final double SUBMENU_Y_OFFSET = 1;

    public static void togglePopup(Popup popup, Node owner) {
        if (popup.isShowing()) {
            popup.hide();
        } else {

            popup.getContent().stream()
                    .map(Node::getUserData)
                    .filter(ViewLifecycle.class::isInstance)
                    .map(ViewLifecycle.class::cast)
                    .forEach(ViewLifecycle::onShow);

            showBelow(popup, owner);
        }
    }

    public static Popup openPopupMenu(Node owner, List<Node> menuItems) {
        VBox box = new VBox();
        box.getStylesheets().add(
                Objects.requireNonNull(PopupMenuUtils.class.getResource("/css/styles.css")).toExternalForm());
        box.getStyleClass().add("popup-content");
        box.getChildren().addAll(menuItems);

        Popup popup = new Popup();
        popup.getContent().add(box);
        popup.setAutoHide(true);

        showRight(popup, owner);

        return popup;
    }

    public static HBox menuRow(String title, Runnable action, Popup popup, Popup submenu) {
        HBox row = new HBox();
        row.setPrefHeight(25);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("popup-button");
        row.setPadding(new Insets(0, 10, 0, 10));

        Label label = new Label(title);
        row.getChildren().add(label);

        row.setOnMouseClicked(e -> {
            action.run();
            if (submenu != null) submenu.hide();
            if (popup != null) popup.hide();
        });

        return row;
    }

    private static void showBelow(Popup popup, Node owner) {
        double x = owner.localToScreen(0, 0).getX();
        double y = owner.localToScreen(0, owner.getBoundsInParent().getHeight()).getY();
        popup.show(owner, x, y);
    }

    private static void showRight(Popup popup, Node anchor) {
        var anchorBounds = anchor.localToScreen(anchor.getBoundsInLocal());

        double x = anchorBounds.getMaxX();
        double y = anchorBounds.getMinY() - SUBMENU_Y_OFFSET;

        popup.show(anchor, x, y);
    }
}