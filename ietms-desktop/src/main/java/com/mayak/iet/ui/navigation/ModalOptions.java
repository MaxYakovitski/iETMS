package com.mayak.iet.ui.navigation;

public record ModalOptions(
        String title,
        String iconPath) {

    public static ModalOptions empty() {
        return new ModalOptions(null, null);
    }
}