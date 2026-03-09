package com.mayak.ietms.ui.navigation;

import javafx.stage.Stage;

public record ModalOptions(
        String title,
        String iconPath,
        Stage ownerStage) {

    public static ModalOptions empty() {
        return new ModalOptions(null, null,   null);
    }
}