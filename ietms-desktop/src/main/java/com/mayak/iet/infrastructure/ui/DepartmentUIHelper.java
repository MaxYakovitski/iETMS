package com.mayak.iet.infrastructure.ui;

import com.mayak.iet.department.dto.DepartmentDto;
import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.ui.core.UserPermissions;
import com.mayak.iet.infrastructure.fx.ComboBoxUtils;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.List;

public class DepartmentUIHelper {

    public static void setupDepartmentSelector(
            UserResponseDto user,
            UserPermissions permissions,
            Label departmentLabel,
            ComboBox<DepartmentDto> departmentComboBox,
            List<DepartmentDto> availableDepartments
    ) {
        if (permissions.isAdmin()) {
            departmentLabel.setVisible(false);
            departmentLabel.setManaged(false);

            departmentComboBox.setVisible(true);
            departmentComboBox.setManaged(true);

            departmentComboBox.getItems().setAll(availableDepartments);
            ComboBoxUtils.setupPrompt(departmentComboBox, DepartmentDto::name);
            return;
        }

        departmentComboBox.setVisible(false);
        departmentComboBox.setManaged(false);

        String departmentName = user.profile() != null ? user.profile().departmentName() : "";

        departmentLabel.setText(departmentName);
        departmentLabel.setVisible(true);
        departmentLabel.setManaged(true);
    }
}