package com.example.bhojhon.controller;

import com.example.bhojhon.util.BaseController;
import javafx.fxml.FXML;

public class MainMenuController extends BaseController {

    @Override
    public void initialize() {
        // No specific init logic needed for menu
    }

    @FXML
    private void handleUserClick() {
        navigateTo("/com/example/bhojhon/user-form-view.fxml");
    }

    @FXML
    private void handleAdminClick() {
        navigateTo("/com/example/bhojhon/admin-login-view.fxml");
    }

    @FXML
    private void handleRestaurantClick() {
        navigateTo("/com/example/bhojhon/restaurant-auth-view.fxml");
    }
}
