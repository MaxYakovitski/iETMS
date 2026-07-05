package com.mayak.ietms.ui.administration;

import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.integration.api.LicenseClient;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.license.dto.LicenseActivateDto;
import com.mayak.ietms.license.dto.LicenseResponseDto;
import com.mayak.ietms.ui.core.ViewLifecycle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.Optional;

/**
 * Controller for the license settings screen.
 * Allows viewing the current license, activating a new one, and deactivating the existing one.
 */
@Controller
@FxmlView("settings_license.fxml")
@Scope("prototype")
@RequiredArgsConstructor
public class LicenseSettingsController implements ViewLifecycle {

    private final LicenseClient licenseClient;

    @FXML
    public VBox licenseInfoBox, licenseEmptyBox;

    @FXML
    public Label companyLabel, maxUsersLabel, expiresAtLabel;

    @FXML
    public Button deactivateButton;

    @FXML
    public TextArea licenseKeyArea;

    @Setter
    private Stage stage;

    @Override
    public void onShow() {
        loadLicense();
    }

    /** Reloads license info from the backend and refreshes the UI state. */
    private void loadLicense() {
        try {
            Optional<LicenseResponseDto> license = licenseClient.getCurrent();
            if (license.isPresent()) {
                LicenseResponseDto dto = license.get();
                companyLabel.setText("company: " + dto.company());
                maxUsersLabel.setText("max users: " + dto.maxUsers());
                expiresAtLabel.setText("expires: " + dto.expiresAt());
                showLicenseInfo();
            } else {
                showEmptyState();
            }
        } catch (ApiException ex) {
            showEmptyState();
        }
    }

    private void showLicenseInfo() {
        licenseInfoBox.setVisible(true);
        licenseInfoBox.setManaged(true);
        licenseEmptyBox.setVisible(false);
        licenseEmptyBox.setManaged(false);
    }

    private void showEmptyState() {
        licenseInfoBox.setVisible(false);
        licenseInfoBox.setManaged(false);
        licenseEmptyBox.setVisible(true);
        licenseEmptyBox.setManaged(true);
    }

    @FXML
    public void handleActivate() {
        String key = licenseKeyArea.getText();
        if (key == null || key.isBlank()) {
            AlertUtils.showError("Please paste a license key.");
            return;
        }
        try {
            licenseClient.activate(new LicenseActivateDto(key.trim()));
            licenseKeyArea.clear();
            loadLicense();
        } catch (ApiException ex) {
            AlertUtils.show(ApiErrorUtils.resolve(ex));
        }
    }

    @FXML
    public void handleDeactivate() {
        boolean ok = AlertUtils.showConfirmation(null,
                "Are you sure you want to deactivate the current license?", stage);
        if (!ok) return;
        try {
            licenseClient.deactivate();
            loadLicense();
        } catch (ApiException ex) {
            AlertUtils.show(ApiErrorUtils.resolve(ex));
        }
    }
}