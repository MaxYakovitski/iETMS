package com.mayak.iet.infrastructure.window;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class HoverSubmenuTracker {

    public static void track(Node anchor, Popup popup) {

        var scene = anchor.getScene();
        if (scene == null) return;

        final var handler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if (!popup.isShowing()) {
                    scene.removeEventFilter(MouseEvent.MOUSE_MOVED, this);
                    return;
                }

                Node content = popup.getContent().getFirst();

                Bounds anchorBounds = anchor.localToScreen(anchor.getBoundsInLocal());
                Bounds popupBounds = content.localToScreen(content.getBoundsInLocal());

                double x = event.getScreenX();
                double y = event.getScreenY();

                boolean insideAnchor =
                        anchorBounds != null && anchorBounds.contains(x, y);

                boolean insidePopup =
                        popupBounds != null && popupBounds.contains(x, y);

                if (!insideAnchor && !insidePopup) {
                    popup.hide();
                    scene.removeEventFilter(MouseEvent.MOUSE_MOVED, this);
                }
            }
        };

        scene.addEventFilter(MouseEvent.MOUSE_MOVED, handler);

        popup.setOnHidden(e ->
                scene.removeEventFilter(MouseEvent.MOUSE_MOVED, handler));
    }
}