package com.mayak.ietms.infrastructure.window;

import com.mayak.ietms.ui.core.ViewLifecycle;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Central service for managing JavaFX windows and stages.
 * Handles opening modal and detached windows, stage lifecycle, and fade animations.
 * <p>
 * Views are loaded via {@link net.rgielen.fxweaver.core.FxWeaver} —
 * controllers must be annotated with {@link net.rgielen.fxweaver.core.FxmlView}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WindowService {

    private final FxWeaver fxWeaver;
    private final Map<WindowKey, Stage> detachedRegistry = new ConcurrentHashMap<>();

    @Getter
    private Stage primaryStage;

    /**
     * Registers the primary application stage.
     * Automatically closes all detached windows when the primary stage is closed.
     *
     * <p>Must be called once during application startup before any window is opened.
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setOnCloseRequest(e -> closeAllDetachedWindows());
    }

    /**
     * Opens a modal window owned by the primary stage.
     *
     * @param controllerClass expected controller type
     * @param initializer     optional callback to configure the controller before display
     * @param title           window title
     * @param iconPath        optional icon resource path, {@code null} to inherit from primary stage
     */
    public <T> void openModalWindow(Class<T> controllerClass,
                                    Consumer<T> initializer,
                                    String title,
                                    String iconPath) {
        try {
            var loaded = fxWeaver.load(controllerClass);
            Parent root = (Parent) loaded.getView().orElseThrow();
            T controller = loaded.getController();
            configureAndShowModalStage(controller, root, initializer, title, iconPath, primaryStage, false);
        } catch (Exception e) {
            log.error("Problem opening {}: {}", controllerClass.getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Opens a modal window with an explicit owner stage.
     * Falls back to the primary stage if {@code owner} is {@code null}.
     *
     * @see #openModalWindow(Class, Consumer, String, String)
     */
    public <T> void openModalWindow(Class<T> controllerClass,
                                    Consumer<T> initializer,
                                    String title,
                                    String iconPath,
                                    Stage owner) {
        try {
            var loaded = fxWeaver.load(controllerClass);
            Parent root = (Parent) loaded.getView().orElseThrow();
            T controller = loaded.getController();
            configureAndShowModalStage(
                    controller, root, initializer, title, iconPath, owner != null ? owner : primaryStage, false
            );
        } catch (Exception e) {
            log.error("Problem opening modal {}: {}", controllerClass.getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Opens a modal window and blocks until it is closed, then returns the controller.
     *
     * @return the controller instance after the window is closed
     */
    public <T> T openModalAndWait(Class<T> controllerClass,
                                  Consumer<T> initializer,
                                  String title,
                                  String iconPath) {
        try {
            var loaded = fxWeaver.load(controllerClass);
            Parent root = (Parent) loaded.getView().orElseThrow();
            T controller = loaded.getController();
            return configureAndShowModalStage(controller, root, initializer, title, iconPath, primaryStage, true);
        } catch (Exception e) {
            log.error("Problem opening {}: {}", controllerClass.getSimpleName(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens a non-modal detached window. If a window with the given key is already open,
     * brings it to the front instead of opening a new one.
     *
     * @param windowKey unique key for deduplication, may be {@code null} to skip tracking
     */
    public <T> void openDetachedWindow(Class<T> controllerClass,
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

            log.info("Opening detached window: {}", controllerClass.getSimpleName());

            var loaded = fxWeaver.load(controllerClass);
            Parent root = (Parent) loaded.getView().orElseThrow();
            T controller = loaded.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.focusedProperty()
                    .addListener((obs, wasFocused, isFocused) -> {
                        if (isFocused) stage.toFront();
            });

            applyDefaultIcon(iconPath, stage, primaryStage);

            injectStageIfSupported(controller, stage);
            if (initializer != null) initializer.accept(controller);

            root.setOpacity(0);

            stage.setOnShown(event -> {
                stage.centerOnScreen();
                fadeIn(stage, 180);
                if (controller instanceof ViewLifecycle lifecycle) lifecycle.onShow();
            });

            stage.setOnHidden(e -> {
                if (controller instanceof ViewLifecycle lifecycle) lifecycle.onHide();
                detachedRegistry.remove(windowKey);
            });

            stage.show();
            if (windowKey != null) detachedRegistry.put(windowKey, stage);
        } catch (Exception e) {
            log.error("Failed to open detached window {}: {}", controllerClass.getSimpleName(), e.getMessage(), e);
        }
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
     * Applies a fade-in animation to the given target (either a {@link Stage} or a {@link javafx.scene.Node}).
     *
     * @param target       the target to animate
     * @param milliseconds animation duration in milliseconds
     * @throws IllegalArgumentException if {@code target} is neither a {@link Stage} nor a {@link Node}
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

    /** Closes all currently open detached windows and clears the registry. */
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

    /** Brings the primary stage to the foreground and requests focus. */
    public void bringPrimaryStageToFront() {
        Platform.runLater(() -> {
            if (primaryStage != null) {
                primaryStage.show();
                primaryStage.toFront();
                primaryStage.requestFocus();
            }
        });
    }

    // Handles both showAndWait (wait=true) and non-blocking show, returns the controller in both cases.
    private <T> T configureAndShowModalStage(T controller,
                                             Parent root,
                                             Consumer<T> initializer,
                                             String title,
                                             String iconPath,
                                             Stage owner,
                                             boolean wait) {

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
            if (stage.getHeight() > bounds.getHeight()) stage.setMaxHeight(bounds.getHeight() - 20);
            if (stage.getWidth() > bounds.getWidth()) stage.setMaxWidth(bounds.getWidth() - 20);
            centerOnScreen(owner, stage);
            fadeIn(stage, 180);
            if (controller instanceof ViewLifecycle lifecycle) lifecycle.onShow();

        });

        stage.setOnHidden(e -> {
            if (controller instanceof ViewLifecycle lifecycle) lifecycle.onHide();
        });

        if (wait) stage.showAndWait();
        else stage.show();

        return controller;
    }

    /**
     * Injects the given {@code stage} into the controller via reflection ({@code setStage(Stage)}).
     * Silently skips if the controller does not have this method.
     */
    private  <T> void injectStageIfSupported(T controller, Stage stage) {
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

    private void applyDefaultIcon(String iconPath, Stage stage, Stage primaryStage) {
        if (iconPath != null) {
            Image icon = new Image(Objects.requireNonNull(getClass().getResource(iconPath)).toString());
            stage.getIcons().setAll(icon);
        } else if (primaryStage != null && !primaryStage.getIcons().isEmpty()) {
            stage.getIcons().setAll(primaryStage.getIcons());
        }
    }

}