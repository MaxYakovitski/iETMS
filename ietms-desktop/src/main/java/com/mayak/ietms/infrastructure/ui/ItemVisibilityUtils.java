package com.mayak.ietms.infrastructure.ui;

import com.mayak.ietms.infrastructure.fx.VisibilityUtils;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ItemVisibilityUtils {

    public static void applyStandardRules(
            TextField customerReference,
            Label customer,
            Label dangerousCheck,
            Label temperature,
            Label weight,
            Label loadingMeters,
            Label rIdLabel,
            TextField rId,
            Label tIdLabel,
            TextField tId,
            Label requestTypeLabel,
            Label priceLabel
    ) {
        VisibilityUtils.hideIfEmpty(customerReference, customer);
        VisibilityUtils.hideIfEmpty(dangerousCheck, temperature);
        VisibilityUtils.hideIfEmpty(weight, loadingMeters);

        if (rIdLabel != null && rId != null) {
            HBox rIdRow = (HBox) rIdLabel.getParent();
            VisibilityUtils.hideContainerIfChildrenEmpty(rIdRow, rId);
        }

        if (tIdLabel != null && tId != null) {
            HBox tIdRow = (HBox) tIdLabel.getParent();
            VisibilityUtils.hideContainerIfChildrenEmpty(tIdRow, tId);
        }

        VisibilityUtils.hideIfEmpty(requestTypeLabel, priceLabel);
    }
}
