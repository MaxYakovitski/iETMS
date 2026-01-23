package com.mayak.iet.infrastructure.ui;

import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.infrastructure.common.TextUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
public class LocationUIHelper {

    public static void addLocationLabels(
            List<LocationDto> locations,
            VBox isoContainer,
            VBox pointContainer
    ) {
        isoContainer.getChildren().clear();
        pointContainer.getChildren().clear();

        if (locations == null || locations.isEmpty()) {
            return;
        }

        addIsoLabels(locations, isoContainer);
        addPointLabels(locations, pointContainer);
    }


    private static void addIsoLabels(List<LocationDto> locations, VBox container) {
        container.getChildren().clear();

        Set<String> uniqueCountryCodes = new LinkedHashSet<>();

        for (LocationDto loc : locations) {
            uniqueCountryCodes.add(loc.countryCode());
        }

        for (String code : uniqueCountryCodes) {
            Label label = new Label(code);
            label.setTextFill(TextUtils.SYSTEM_TEXT_DEFAULT_COLOR);
            container.getChildren().add(label);
        }
    }

    private static void addPointLabels(List<LocationDto> locations, VBox pointContainer) {
        pointContainer.getChildren().clear();

        for (LocationDto loc : locations) {
            Label label = new Label(formatPoint(loc));
            label.setTextFill(TextUtils.SYSTEM_TEXT_DEFAULT_COLOR);
            LabelTooltipUtils.attachAutoTooltip(label);
            pointContainer.getChildren().add(label);
        }
    }

    public static String formatPoint(LocationDto loc) {

        if (loc.placeName() == null) {
            return loc.countryCode() + ", " + loc.zipCode();
        }
        return loc.countryCode() + ", " + loc.zipCode() + ", " + loc.placeName();
    }
}