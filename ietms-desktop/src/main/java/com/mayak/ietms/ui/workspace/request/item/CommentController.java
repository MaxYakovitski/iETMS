package com.mayak.ietms.ui.workspace.request.item;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@FxmlView("request_item_comment.fxml")
@Scope("prototype")
public class CommentController {

    @FXML
    private TextArea commentsTextArea;

    public void setCommentsText(String comments) {
        if (comments != null) commentsTextArea.setText(comments);
    }
}