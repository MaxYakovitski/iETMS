package com.mayak.ietms.infrastructure.ui;

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ClipboardUtils {

    /**
     * Binds a click-to-copy behaviour to the given labels.
     * Each label gets a {@link Cursor#HAND} cursor and copies its text to the system clipboard on click.
     *
     * @param labels labels to enable copy-on-click for
     */
    public static void copyOnClick(Label... labels) {
        for (Label label : labels) {
            label.setCursor(Cursor.HAND);
            label.setOnMouseClicked(e -> {
                String text = label.getText();
                if (text == null || text.isBlank()) return;
                ClipboardContent content = new ClipboardContent();
                content.putString(text);
                Clipboard.getSystemClipboard().setContent(content);
            });
        }
    }
}