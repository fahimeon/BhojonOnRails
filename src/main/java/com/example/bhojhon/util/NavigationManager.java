package com.example.bhojhon.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NavigationManager {
    private static NavigationManager instance;
    private Stage primaryStage;
    private final Map<String, Object> sharedData;
    private final java.util.Stack<NavigationHistoryItem> history;
    private String currentFxmlPath;
    private String currentTitle;

    private NavigationManager() {
        sharedData = new HashMap<>();
        history = new java.util.Stack<>();
    }

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void navigateTo(String fxmlPath, String title) {
        // Push current screen to history before navigating away
        if (currentFxmlPath != null && currentTitle != null) {
            history.push(new NavigationHistoryItem(currentFxmlPath, currentTitle));
        }

        loadView(fxmlPath, title);
    }

    /**
     * Internal method to actually load the view and update current state tracker
     */
    private void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Get controller and pass navigation manager reference
            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setNavigationManager(this);
            }

            Scene scene = primaryStage.getScene();

            // Capture current window state just in case
            boolean isMaximized = primaryStage.isMaximized();

            if (scene == null) {
                scene = new Scene(root);
                // Load global CSS theme (if present)
                URL cssUrl = getClass().getResource("/com/example/bhojhon/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    System.err.println("Warning: styles.css not found; UI will use default styling.");
                }
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            primaryStage.setTitle(title);
            primaryStage.show();

            // Restore maximized state if it was somehow lost (safeguard)
            if (isMaximized && !primaryStage.isMaximized()) {
                primaryStage.setMaximized(true);
            }

            // Update current state
            this.currentFxmlPath = fxmlPath;
            this.currentTitle = title;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + fxmlPath);
        }
    }

    public void navigateTo(String fxmlPath, String title, double width, double height) {
        navigateTo(fxmlPath, title);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
    }

    /**
     * Navigate back to the previous screen
     */
    public void goBack() {
        if (!history.isEmpty()) {
            NavigationHistoryItem previousParams = history.pop();
            // Load previous view WITHOUT pushing current view to history
            // We also need to update currentFxmlPath/currentTitle to the popped one
            loadView(previousParams.fxmlPath, previousParams.title);
        } else {
            System.out.println("History is empty, cannot go back.");
        }
    }

    public void clearHistory() {
        history.clear();
        currentFxmlPath = null;
        currentTitle = null;
    }

    public void putData(String key, Object value) {
        sharedData.put(key, value);
    }

    public Object getData(String key) {
        return sharedData.get(key);
    }

    public void removeData(String key) {
        sharedData.remove(key);
    }

    public void clearData() {
        sharedData.clear();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Helper class to store navigation history
     */
    private static class NavigationHistoryItem {
        String fxmlPath;
        String title;

        public NavigationHistoryItem(String fxmlPath, String title) {
            this.fxmlPath = fxmlPath;
            this.title = title;
        }
    }
}
