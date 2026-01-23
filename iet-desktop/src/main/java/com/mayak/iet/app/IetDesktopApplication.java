package com.mayak.iet.app;

import com.mayak.iet.config.YamlPropertySourceFactory;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.ui.auth.LoginController;
import com.mayak.iet.infrastructure.error.AlertUtils;
import com.mayak.iet.infrastructure.window.WindowService;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Locale;
import java.util.Objects;

@Configuration
@ComponentScan("com.mayak.iet")
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
@Slf4j
public class IetDesktopApplication extends javafx.application.Application {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        Locale.setDefault(Locale.UK);
        springContext = new AnnotationConfigApplicationContext(IetDesktopApplication.class);
        log.info("Spring context initialized");
    }

    @Override
    public void start(Stage primaryStage) {

        WindowService windowService = springContext.getBean(WindowService.class);
        windowService.setPrimaryStage(primaryStage);
        AlertUtils.setWindowService(windowService);

        try {
            WindowService.Loaded<LoginController> loaded =
                    windowService.loadControllerWithNode(View.LOGIN.getPath(), LoginController.class);

            Scene loginScene = new Scene(loaded.node());

            Image icon = new Image(Objects.requireNonNull(getClass().getResource("/icons/icon.png")).toString());
            primaryStage.getIcons().add(icon);
            primaryStage.setTitle("iETMS");

            primaryStage.setScene(loginScene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            log.error("Failed to load LoginController FXML", e);
        }
    }

    @Override
    public void stop() {
        springContext.close();
        log.info("Spring context closed");
    }

    public static ConfigurableApplicationContext getContext() {
        return springContext;
    }
}