package com.mayak.ietms.infrastructure.ui;

import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.infrastructure.common.TextUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
public class LocationUIHelper {

    /**
     * Populates ISO country code and point labels into the provided containers.
     * Both containers are cleared before population.
     * Does nothing if {@code locations} is null or empty.
     *
     * @param locations      locations to render; may be null
     * @param isoContainer   container for unique country code labels
     * @param pointContainer container for formatted point labels with tooltip and copy-on-click
     */
    public static void addLocationLabels(List<LocationDto> locations, VBox isoContainer, VBox pointContainer) {
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
            ClipboardUtils.copyOnClick(label);
            pointContainer.getChildren().add(label);
        }
    }

    /**
     * Formats a location as {@code "CC, ZIP"} or {@code "CC, ZIP, city"} if place name is present.
     *
     * @param loc location to format
     * @return formatted string representation
     */
    public static String formatPoint(LocationDto loc) {
        if (loc.placeName() == null) {
            return loc.countryCode() + ", " + loc.zipCode();
        }
        return loc.countryCode() + ", " + loc.zipCode() + ", " + loc.placeName();
    }
}