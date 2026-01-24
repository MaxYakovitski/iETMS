package com.mayak.iet.ui.user;

import com.mayak.iet.integration.api.UserStatisticsClient;
import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.infrastructure.common.TextUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @FXML public Label userLabel, userTypeLabel, userRoleLabel, departmentLabel,
            monthLabel, placedLabel, joinedLabel, bidedLabel, dispatchedLabel;
    @FXML public HBox roleContainer, departmentContainer;

    @Getter private Stage stage;
    @Getter private UserResponseDto user;

    private final UserStatisticsClient statisticsClient;

    public void init(Stage stage, UserResponseDto user) {
        this.stage = stage;
        this.user = user;

        fillView();
        loadStatistics();
    }

    private void fillView() {
        if (user == null) {
            log.warn("UserController initialized with null user");
            return;
        }

        userLabel.setText(String.format("%s %s", user.name(), user.surname()));
        userTypeLabel.setText(user.userType().name());

        monthLabel.setText(LocalDate.now().format(TextUtils.MONTH_STANDALONE));

        placedLabel.setText("...");
        joinedLabel.setText("...");
        bidedLabel.setText("...");
        dispatchedLabel.setText("...");

        applyDepartmentView();
        applyRoleView();
    }

    private void applyDepartmentView() {
        if (user.profile() == null || user.profile().departmentName() == null) {
            departmentContainer.setManaged(false);
            departmentContainer.setVisible(false);
            return;
        }

        departmentLabel.setText(user.profile().departmentName());
    }

    private void applyRoleView() {
        if (user.profile() == null || user.profile().role() == null) {
            roleContainer.setManaged(false);
            roleContainer.setVisible(false);
            return;
        }

        userRoleLabel.setText(user.profile().role().name());
    }

    private void loadStatistics() {
        try {
            var stats = statisticsClient.getCurrentMonthStats(user.id());

            placedLabel.setText(String.valueOf(stats.placed()));
            joinedLabel.setText(String.valueOf(stats.joined()));
            bidedLabel.setText(String.valueOf(stats.bided()));
            dispatchedLabel.setText(String.valueOf(stats.assigned()));

        } catch (Exception e) {
            log.error("Failed to load personal statistics for user {}", user.id(), e);

            placedLabel.setText("–");
            joinedLabel.setText("–");
            bidedLabel.setText("–");
            dispatchedLabel.setText("–");
        }
    }
}