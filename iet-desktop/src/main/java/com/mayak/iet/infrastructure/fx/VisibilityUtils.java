package com.mayak.iet.infrastructure.fx;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;

public class VisibilityUtils {

    public static void hideIfEmpty(Node... nodes) {
        for (Node node : nodes) {
            boolean show = isNodeNonEmpty(node);

            node.setVisible(show);
            node.setManaged(show);
        }
    }

    private static boolean isNodeNonEmpty(Node node) {
        if (node instanceof Label label) {
            return label.getText() != null && !label.getText().isBlank();
        }
        if (node instanceof TextInputControl text) {
            return text.getText() != null && !text.getText().isBlank();
        }

        return true;
    }


    public static void hideContainerIfChildrenEmpty(Pane container, Node... children) {
        boolean anyVisible = false;

        for (Node node : children) {
            if (isNodeNonEmpty(node)) {
                anyVisible = true;
                break;
            }
        }

        container.setVisible(anyVisible);
        container.setManaged(anyVisible);
    }
}