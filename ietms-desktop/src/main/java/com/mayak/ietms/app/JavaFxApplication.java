package com.mayak.ietms.app;

import com.mayak.ietms.DesktopApplication;
import com.mayak.ietms.auth.SessionService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Locale;

@Slf4j
public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        Locale.setDefault(Locale.UK);
        springContext = new SpringApplicationBuilder(DesktopApplication.class).headless(false).run();
    }

    @Override
    public void start(Stage primaryStage) {
        springContext.getBean(AppNavigator.class).showLogin();
    }

    @Override
    public void stop() {
        try {
            springContext.getBean(AppNavigator.class).closeAll();
            springContext.getBean(SessionService.class).logout();
        } catch (Exception ignored) {}
        springContext.close();
        Platform.exit();
    }

}
