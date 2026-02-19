package com.mayak.ietms.infrastructure.window;

import com.mayak.ietms.ui.connection.ConnectionOverlayController;
import com.mayak.ietms.ui.core.ViewLifecycle;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class WindowService {

    private final ApplicationContext applicationContext;
    private final Map<WindowKey, Stage> detachedRegistry = new ConcurrentHashMap<>();

    private Parent connectionOverlay;
    private ConnectionOverlayController connectionOverlayController;

    @Getter @Setter
    private Stage primaryStage;

    @Setter
    private Runnable loginCallback;

    public <T> void openModalWindow(String fxmlPath,
                                    Class<T> controllerClass,
                                    Consumer<T> initializer,
                                    String title,
                                    String iconPath) {
        try {
            Loaded<T> loaded = loadView(fxmlPath, controllerClass);
            Stage owner = primaryStage;

            configureAndShowModalStage(
                    loaded, initializer, title, iconPath,
                    owner, false
            );

        } catch (Exception e) {
            log.error("Problem with window {}: {}", fxmlPath, e.getMessage(), e);
        }
    }

    public <T> void openModalWindow(String fxmlPath,
                                    Class<T> controllerClass,
                                    Consumer<T> initializer,
                                    String title,
                                    String iconPath,
                                    Stage owner) {
        try {
            Loaded<T> loaded = loadView(fxmlPath, controllerClass);

            configureAndShowModalStage(
                    loaded, initializer, title, iconPath,
                    owner != null ? owner : primaryStage,
                    false
            );

        } catch (Exception e) {
            log.error("Problem with modal {}: {}", fxmlPath, e.getMessage(), e);
        }
    }

    public <T> T openModalAndWait(String fxmlPath,
                                  Class<T> controllerClass,
                                  Consumer<T> initializer,
                                  String title,
                                  String iconPath) {
        try {
            Loaded<T> loaded = loadView(fxmlPath, controllerClass);

            return configureAndShowModalStage(
                    loaded, initializer, title, iconPath,
                    primaryStage,
                    true
            );

        } catch (Exception e) {
            log.error("Problem with window {}: {}", fxmlPath, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> void openDetachedWindow(
            String fxmlPath,
            Class<T> controllerClass,
            Consumer<T> initializer,
            String title,
            String iconPath,
            WindowKey windowKey
    ) {
        try {

            if (windowKey != null) {
                Stage existing = detachedRegistry.get(windowKey);
                if (existing != null) {
                    log.info("Detached window already open: {}", windowKey);
                    existing.toFront();
                    existing.requestFocus();
                    return;
                }
            }

            log.info("Opening detached window: {}", fxmlPath);

            Loaded<T> loaded = loadView(fxmlPath, controllerClass);
            Parent root = loaded.node();
            T controller = loaded.controller();

            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.initOwner(primaryStage);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(true);

            double fixedWidth = 1520;
            stage.setWidth(fixedWidth);
            stage.setMinWidth(fixedWidth);
            stage.setMaxWidth(fixedWidth);

            double minHeight = 520;
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
            stage.setMinHeight(minHeight);
            stage.setMaxHeight(screenHeight);
            stage.setHeight(screenHeight);

            stage.fullScreenProperty().addListener((value, oldValue, newValue) -> {
                if (newValue) stage.setFullScreen(false);
            });

            injectStageIfSupported(controller, stage);
            if (initializer != null) initializer.accept(controller);

            stage.setOnShown(event -> {
                centerRelativeToOwner(stage);
                fadeIn(stage, 180);

                if (controller instanceof ViewLifecycle lifecycle) {
                    lifecycle.onShow();
                }
            });

            stage.setOnHidden(e -> {
                if (controller instanceof ViewLifecycle lifecycle) {
                    lifecycle.onHide();
                }
                detachedRegistry.remove(windowKey);
            });

            stage.show();
            if (windowKey != null) {
                detachedRegistry.put(windowKey, stage);
            }

        } catch (IOException e) {
            log.error("Failed to open detached window {}: {}", fxmlPath, e.getMessage(), e);
        }
    }

    // ------------------ CORE REFACTORED METHOD ------------------
    private <T> T configureAndShowModalStage(
            Loaded<T> loaded,
            Consumer<T> initializer,
            String title,
            String iconPath,
            Stage owner,
            boolean wait
    ) {
        Parent root = loaded.node();
        T controller = loaded.controller();

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);

        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.sizeToScene();
        stage.setResizable(false);

        injectStageIfSupported(controller, stage);
        if (initializer != null) initializer.accept(controller);

        if (iconPath != null) {
            Image icon = new Image(Objects.requireNonNull(getClass().getResource(iconPath)).toString());
            stage.getIcons().setAll(icon);
        } else if (owner != null && !owner.getIcons().isEmpty()) {
            stage.getIcons().setAll(owner.getIcons());
        }

        stage.setOnShown(event -> {
            centerOnScreen(stage);
            fadeIn(stage, 180);
        });

        if (wait) {
            stage.showAndWait();
        } else {
            stage.show();
        }
        return controller;
    }

    // ------------------ STAGE INJECTION ------------------
    public <T> void injectStageIfSupported(T controller, Stage stage) {
        if (controller == null) return;
        try {
            Method method = controller.getClass().getMethod("setStage", Stage.class);
            method.invoke(controller, stage);
            log.debug("Stage injected into {}", controller.getClass().getSimpleName());
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            log.error("Could not inject Stage into {}", controller.getClass().getSimpleName(), e);
        }
    }

    // ------------------ LOAD WITH NODE ------------------
    public record Loaded<T>(Parent node, T controller) {}

    public <T> Loaded<T> loadControllerWithNode(String fxmlPath, Class<T> controllerClass) {
        try {
            return loadView(fxmlPath, controllerClass);
        } catch (IOException e) {
            log.error("Failed to load FXML file: {}", fxmlPath, e);
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    public <T> Loaded<T> loadControllerWithNode(String fxmlPath) {
        Loaded<T> loaded = getLoaded(fxmlPath);
        fadeIn(loaded.node(), 360);
        return loaded;
    }

    // ------------------ POSITIONING & ANIMATION ------------------
    public void centerOnScreen(Stage modalStage) {
        if (primaryStage != null) {
            modalStage.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - modalStage.getWidth() / 2);
            modalStage.setY(primaryStage.getY() + primaryStage.getHeight() / 2 - modalStage.getHeight() / 2);
        } else {
            modalStage.centerOnScreen();
        }
    }

    public void centerRelativeToOwner(Stage stage) {
        Window owner = stage.getOwner();
        if (owner instanceof Stage ownerStage) {
            stage.setX(ownerStage.getX() + (ownerStage.getWidth() - stage.getWidth()) / 2);
            stage.setY(ownerStage.getY() + (ownerStage.getHeight() - stage.getHeight()) / 2);
        } else {
            centerOnScreen(stage);
        }
    }

    public void fadeIn(Object target, Integer milliseconds) {
        Node nodeToAnimate = null;

        try {
            if (target instanceof Stage stage) {
                if (stage.getScene() != null) {
                    nodeToAnimate = stage.getScene().getRoot();
                } else if (stage.getUserData() instanceof Node root){
                    nodeToAnimate = root;
                } else {
                    throw new IllegalArgumentException("fadeIn: Stage has no scene or root node to animate");
                }
            } else if (target instanceof Node node) {
                nodeToAnimate = node;
            }
            if (nodeToAnimate == null)
                throw new IllegalArgumentException("fadeIn: target must be either Stage or Node");

            FadeTransition fadeIn = new FadeTransition(Duration.millis(milliseconds), nodeToAnimate);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } catch (Exception e) {
            log.error("Failed to animate fadeIn", e);
        }
    }

    // ------------------ INTERNAL LOAD HELPERS ------------------
    @NotNull
    private <T> Loaded<T> getLoaded(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            T controller = loader.getController();
            return new Loaded<>(root, controller);
        } catch (IOException e) {
            log.error("Failed to load FXML file: {}", fxmlPath, e);
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    private <T> Loaded<T> loadView(String fxmlPath, Class<T> controllerClass) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Object rawController = loader.getController();
        @SuppressWarnings("unchecked")
        T controller = controllerClass != null
                ? controllerClass.cast(rawController)
                : (T) rawController;
        return new Loaded<>(root, controller);
    }

    public void showLoading() {
        Platform.runLater(() -> {
            initConnectionOverlay();
            connectionOverlayController.showLoading();
            connectionOverlay.setVisible(true);
        });
    }

    public void showBackendUnavailable() {
        Platform.runLater(() -> {
            closeAllDetachedWindows();
            bringPrimaryStageToFront();
            Platform.runLater(() -> {
                initConnectionOverlay();
                connectionOverlayController.showDisconnected();
                connectionOverlay.setVisible(true);
            });
        });
    }

    public void hideBlockingOverlay() {
        Platform.runLater(() -> {
            if (connectionOverlay != null) {
                connectionOverlay.setVisible(false);
            }
        });
    }

    private void initConnectionOverlay() {
        if (connectionOverlay != null) return;

        Loaded<ConnectionOverlayController> loaded =
                loadControllerWithNode("/fxml/connection_overlay.fxml");

        connectionOverlay = loaded.node();
        connectionOverlayController = loaded.controller();

        Scene scene = primaryStage.getScene();
        Parent root = scene.getRoot();

        if (root instanceof StackPane stack) {
            stack.getChildren().add(connectionOverlay);
        } else {
            StackPane wrapper = new StackPane(root, connectionOverlay);
            scene.setRoot(wrapper);
        }
    }

    public void closeAllDetachedWindows() {
        Platform.runLater(() -> {
            detachedRegistry.values().forEach(stage -> {
                try {
                    stage.close();
                } catch (Exception ignored) {}
            });
            detachedRegistry.clear();
        });
    }

    public void bringPrimaryStageToFront() {
        Platform.runLater(() -> {
            if (primaryStage != null) {
                primaryStage.show();
                primaryStage.toFront();
                primaryStage.requestFocus();
            }
        });
    }

    public void forceLogout() {
        Platform.runLater(() -> {

            for (Window w : Window.getWindows()) {
                if (w instanceof Stage s) {
                    try { s.close(); } catch (Exception ignored) {}
                }
            }

            if (loginCallback != null) {
                loginCallback.run();
            }
        });
    }
}
