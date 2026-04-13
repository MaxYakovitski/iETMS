package com.mayak.ietms.ui.workspace.planner.view;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Objects;

public class TimelineToggleButton extends Button {

    private final Image iconRight;
    private final Image iconDown;
    private boolean expanded = false;

    public TimelineToggleButton() {
        iconRight = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/right-chevron.png")));
        iconDown = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/down-chevron.png")));
        getStyleClass().add("timeline-toggle-button");
        ImageView iv = new ImageView(iconRight);
        iv.setFitWidth(20);
        iv.setFitHeight(20);
        iv.setPreserveRatio(true);
        iv.setPickOnBounds(true);
        setGraphic(iv);
    }

    public boolean toggle() {
        expanded = !expanded;
        ((ImageView) getGraphic()).setImage(expanded ? iconDown : iconRight);
        return expanded;
    }

    public void reset() {
        expanded = false;
        ((ImageView) getGraphic()).setImage(iconRight);
    }
}