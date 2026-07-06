package com.example.bhojhon.controller;

import com.example.bhojhon.util.BaseController;
import javafx.fxml.FXML;

/**
 * Controller for the Welcome/Splash screen.
 * Demonstrates INHERITANCE by extending BaseController.
 */
public class WelcomeController extends BaseController {

    /**
     * Initialize method called automatically by JavaFX
     */
    @Override
    public void initialize() {
        // No initialization needed for welcome screen
    }

    /**
     * Handle Start button click
     */
    @FXML
    private void handleStartButton() {
        goToTrainSearch();
    }
}
