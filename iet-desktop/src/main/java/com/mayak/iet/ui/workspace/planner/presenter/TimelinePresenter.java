package com.mayak.iet.ui.workspace.planner.presenter;

import com.mayak.iet.domain.planner.timeline.TimelineColor;
import com.mayak.iet.domain.planner.timeline.TimelineEntry;
import com.mayak.iet.ui.workspace.planner.item.TimelineItem;
import com.mayak.iet.infrastructure.common.TextUtils;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimelinePresenter {

    public void render(VBox container, List<TimelineEntry> entries) {
        container.getChildren().clear();

        for (int i = 0; i < entries.size(); i++) {
            TimelineEntry e = entries.get(i);

            boolean isFirst = i == 0;
            boolean isLast  = i == entries.size() - 1;
            boolean single  = entries.size() == 1;

            boolean showTopDot    = !single && isLast;
            boolean showBottomDot = isFirst;
            boolean showLine      = !isLast;


            container.getChildren().add(
                    new TimelineItem(e.label(), e.time(), mapColor(e.color()), showTopDot, showBottomDot, showLine));
        }
    }

    private Color mapColor(TimelineColor color) {
        return switch (color) {
            case SUCCESS -> TextUtils.SYSTEM_TEXT_GREEN_COLOR;
            case ERROR -> TextUtils.SYSTEM_TEXT_RED_COLOR;
        };
    }
}