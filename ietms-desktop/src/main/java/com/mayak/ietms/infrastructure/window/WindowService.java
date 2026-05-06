package com.mayak.ietms.infrastructure.window;

import com.mayak.ietms.ui.connection.ConnectionOverlayController;
import com.mayak.ietms.ui.core.ViewLifecycle;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Central service for managing JavaFX windows and stages.
 * Handles opening modal and detached windows, stage lifecycle, fade animations,
 * connection overlays, and forced logout.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WindowService {

    private final ApplicationContext applicationContext;
    private final Map<WindowKey, Stage> detachedRegistry = new ConcurrentHashMap<>();

    private Parent connectionOverlay;
    private ConnectionOverlayController connectionOverlayController;

    @Getter private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setOnCloseRequest(e -> closeAllDetachedWindows());
    }

    @Setter private Runnable loginCallback;

    /**
     * Opens a modal window owned by the primary stage.
     *
     * @param fxmlPath        path to the FXML resource
     * @param controllerClass expected controller type
     * @param initializer     optional callback to configure the controller before display
     * @param title           window title
     * @param iconPath        optional icon resource path, {@code null} to inherit from primary stage
     */
    public <T> void openModalWindow(String fxmlPath,
                                    Class<T> controllerClass,
                                    Consumer<T> initializer,
                                    String title,
                                    String iconPath) {
        try {
            Loaded<T> loaded = loadView(fxmlPath, controllerClass);
            Stage owner = primaryStage;

            configureAndShowModalStage(loaded, initializer, title, iconPath, owner, false);

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

    /**
     * Opens a modal window and blocks until it is closed, then returns the controller.
     *
     * @return the controller instance after the window is closed
     */
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

    /**
     * Opens a non-modal detached window. If a window with the given key is already open,
     * brings it to the front instead of opening a new one.
     *
     * @param windowKey unique key for deduplication, may be {@code null} to skip tracking
     */
    public <T> void openDetachedWindow(
            String fxmlPath,
            Class<T> controllerClass,
            Consumer<T> initializer,
            String title,
            String iconPath,
            WindowKey windowKey) {
        try {

            if (windowKey != null) {
                Stage existing = detachedRegistry.get(windowKey);
                if (existing != null) {
                    log.info("Detached window already open: {}", windowKey);
                    Platform.runLater(() -> {
                        if (existing.isIconified()) existing.setIconified(false);
                        existing.show();
                        existing.toFront();
                        existing.requestFocus();
                    });
                    return;
                }
            }

            log.info("Opening detached window: {}", fxmlPath);

            Loaded<T> loaded = loadView(fxmlPath, controllerClass);
            Parent root = loaded.node();
            T controller = loaded.controller();

            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(true);

            stage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) {
                    stage.toFront();
                }
            });

            applyDefaultIcon(iconPath, stage, primaryStage);

            injectStageIfSupported(controller, stage);
            if (initializer != null) initializer.accept(controller);

            root.setOpacity(0);
            stage.setOnShown(event -> {
                stage.centerOnScreen();
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
            Loaded<T> loaded, Consumer<T> initializer,
            String title, String iconPath,
            Stage owner,
            boolean wait) {
        Parent root = loaded.node();
        T controller = loaded.controller();

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);

        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.setResizable(false);
        stage.sizeToScene();

        applyDefaultIcon(iconPath, stage, owner);
        injectStageIfSupported(controller, stage);
        if (initializer != null) initializer.accept(controller);

        root.setOpacity(0);
        stage.setOnShown(e -> {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            if (stage.getHeight() > bounds.getHeight()) {
                stage.setMaxHeight(bounds.getHeight() - 20);
            }
            if (stage.getWidth() > bounds.getWidth()) {
                stage.setMaxWidth(bounds.getWidth() - 20);
            }
            centerOnScreen(owner, stage);
            fadeIn(stage, 180);

            if (controller instanceof ViewLifecycle lifecycle) {
                lifecycle.onShow();
            }
        });

        stage.setOnHidden(e -> {
            if (controller instanceof ViewLifecycle lifecycle) {
                lifecycle.onHide();
            }
        });

        if (wait) stage.showAndWait();
        else stage.show();

        return controller;
    }

    /**
     * Centers {@code stage} relative to {@code owner}.
     * Has no effect if {@code owner} is {@code null}.
     */
    public void centerOnScreen(Stage owner, Stage stage) {
        if (owner != null) {
            stage.setX(owner.getX() + (owner.getWidth()  - stage.getWidth())  / 2.0);
            stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2.0);
        }
    }

    /**
     * Injects the given {@code stage} into the controller via reflection ({@code setStage(Stage)}).
     * Silently skips if the controller does not have this method.
     */
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
        fadeIn(loaded.node(), 180);
        return loaded;
    }

    /**
     * Applies a fade-in animation to the given target (either a {@link Stage} or a {@link javafx.scene.Node}).
     *
     * @param target       the target to animate
     * @param milliseconds animation duration in milliseconds
     */
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

            var windows = new ArrayList<>(Window.getWindows());

            for (Window w : windows) {
                if (w instanceof Stage s) {
                    try {
                        s.close();
                    } catch (Exception ignored) {}
                }
            }
            if (loginCallback != null) {
                loginCallback.run();
            }
        });
    }

    private void applyDefaultIcon(String iconPath, Stage stage, Stage primaryStage) {
        if (iconPath != null) {
            Image icon = new Image(
                    Objects.requireNonNull(getClass().getResource(iconPath)).toString()
            );
            stage.getIcons().setAll(icon);
        } else if (primaryStage != null && !primaryStage.getIcons().isEmpty()) {
            stage.getIcons().setAll(primaryStage.getIcons());
        }
    }
}