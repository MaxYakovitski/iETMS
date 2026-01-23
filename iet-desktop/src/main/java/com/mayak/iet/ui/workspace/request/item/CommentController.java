package com.mayak.iet.ui.workspace.request.item;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
public class CommentController {

    @FXML
    private TextArea commentsTextArea;

    public void setCommentsText(String comments) {
        if (comments != null) commentsTextArea.setText(comments);
    }
}