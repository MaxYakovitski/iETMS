package com.mayak.ietms.infrastructure.ui;

import com.mayak.ietms.infrastructure.fx.VisibilityUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ItemVisibilityUtils {

    /**
     * Applies standard label visibility rules for request and shipment list items.
     * Labels and their paired siblings are hidden when empty.
     * ID rows are hidden via their parent {@link HBox} when the value label is empty.
     *
     * @param rIdLabel      may be {@code null} — row is skipped if either rIdLabel or rId is null
     * @param rId           may be {@code null}
     * @param tIdLabel      may be {@code null} — row is skipped if either tIdLabel or tId is null
     * @param tId           may be {@code null}
     */
    public static void applyStandardRules(
            Label customerReference,
            Label customer,
            Label dangerousCheck,
            Label temperature,
            Label weight,
            Label loadingMeters,
            Label rIdLabel,
            Label rId,
            Label tIdLabel,
            Label tId,
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