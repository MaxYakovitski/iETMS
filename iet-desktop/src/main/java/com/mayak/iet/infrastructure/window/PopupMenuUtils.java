package com.mayak.iet.infrastructure.window;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.List;
import java.util.Objects;

public class PopupMenuUtils {

    public static Button menuButton(String title, Runnable action, Popup popup, Popup submenu) {
        Button button = new Button(title);
        button.getStyleClass().add("popup-button");
        button.setOnAction(e -> {
            action.run();
            if (submenu != null) submenu.hide();
            if (popup != null) popup.hide();
        });
        return button;
    }

    private static final Image ARROW_RIGHT = new Image(
            Objects.requireNonNull(PopupMenuUtils.class.getResource("/icons/arrow_right.png")).toExternalForm()
    );
    private static final Image ARROW_DOWN = new Image(
            Objects.requireNonNull(PopupMenuUtils.class.getResource("/icons/arrow_down.png")).toExternalForm()
    );

    public static void setArrowClosed(ImageView icon) {
        icon.setImage(ARROW_RIGHT);
    }

    public static void setArrowOpen(ImageView icon) {
        icon.setImage(ARROW_DOWN);
    }

    public static Popup openPopupMenu(
            ImageView arrowIcon,
            Node anchor,
            List<Button> menuButtons
    ) {
        setArrowOpen(arrowIcon);

        VBox box = new VBox();
        box.getStylesheets().add(
                Objects.requireNonNull(PopupMenuUtils.class.getResource("/css/styles.css")).toExternalForm());
        box.getStyleClass().add("popup-content");

        box.getChildren().addAll(menuButtons);

        Popup popup = new Popup();
        popup.getContent().add(box);
        popup.setAutoHide(true);

        popup.setOnHidden(e -> setArrowClosed(arrowIcon));

        var screenPos = anchor.localToScreen(0, 0);
        popup.show(anchor, screenPos.getX() + 10, screenPos.getY() - 15);

        return popup;
    }
}