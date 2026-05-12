package com.mayak.ietms.ui.dashboard;

import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

/**
 * Controller for the main dashboard view ({@code dashboard.fxml}).
 *
 * <p>Currently acts as a view marker only — the dashboard layout is purely
 * declarative (defined in FXML) and requires no controller logic.
 * Registered as a Spring bean so that {@link net.rgielen.fxweaver.core.FxWeaver}
 * can resolve and inject it via {@link net.rgielen.fxweaver.core.FxmlView}.
 */
@Controller
@FxmlView("dashboard.fxml")
public class DashboardController {}