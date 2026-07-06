package com.example.bhojhon.controller;

import com.example.bhojhon.data.CartManager;
import com.example.bhojhon.model.CartItem;
import com.example.bhojhon.util.BaseController;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;

/**
 * Controller for the Shopping Cart screen.
 * Displays cart items and allows order confirmation.
 */
public class CartController extends BaseController {

    @FXML
    private TableView<CartItem> cartTableView;

    @FXML
    private TableColumn<CartItem, String> itemNameColumn;

    @FXML
    private TableColumn<CartItem, Double> priceColumn;

    @FXML
    private TableColumn<CartItem, Integer> quantityColumn;

    @FXML
    private TableColumn<CartItem, Double> subtotalColumn;

    @FXML
    private Label totalLabel;

    @Override
    public void initialize() {
        // Set up table columns
        itemNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFoodItem().getName()));

        priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getFoodItem().getPrice()).asObject());
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("৳%.0f", price));
                }
            }
        });

        // Quantity Column with + / - buttons
        quantityColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));

        quantityColumn.setCellFactory(column -> new TableCell<>() {
            private final Button minusBtn = new Button("-");
            private final Button plusBtn = new Button("+");
            private final Label qtyLabel = new Label();
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, minusBtn, qtyLabel, plusBtn);

            {
                pane.setAlignment(Pos.CENTER);

                // Button Logic
                minusBtn.setOnAction(event -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null) {
                        updateQuantity(item, item.getQuantity() - 1);
                    }
                });
                plusBtn.setOnAction(event -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null) {
                        updateQuantity(item, item.getQuantity() + 1);
                    }
                });

                // Style buttons (Circular)
                String baseBtnStyle = "-fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; "
                        +
                        "-fx-background-radius: 50%; -fx-font-weight: bold; -fx-padding: 0; -fx-cursor: hand;";

                minusBtn.setStyle(baseBtnStyle
                        + "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-border-color: #ef9a9a; -fx-border-radius: 50%;");
                plusBtn.setStyle(baseBtnStyle
                        + "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-border-color: #a5d6a7; -fx-border-radius: 50%;");

                // Style Label
                qtyLabel.setStyle(
                        "-fx-font-weight: bold; -fx-font-size: 14px; -fx-alignment: CENTER; -fx-text-fill: black;");
                qtyLabel.setMinWidth(30); // Fixed width to prevent jumping
                qtyLabel.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    qtyLabel.setText(String.valueOf(item));
                    setGraphic(pane);
                }
            }
        });

        subtotalColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getSubtotal()).asObject());
        subtotalColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                if (empty || subtotal == null) {
                    setText(null);
                } else {
                    setText(String.format("৳%.0f", subtotal));
                }
            }
        });

        // Load cart items
        cartTableView.setItems(CartManager.getInstance().getCartItems());

        // Update total
        updateTotal();

        // Listen for cart changes to update total
        CartManager.getInstance().getCartItems().addListener(
                (javafx.collections.ListChangeListener.Change<? extends CartItem> c) -> {
                    updateTotal();
                });
    }

    /**
     * Update item quantity
     */
    private void updateQuantity(CartItem item, int newQuantity) {
        CartManager.getInstance().updateQuantity(item, newQuantity);
        cartTableView.refresh(); // Refresh table to show new subtotal/quantity
        updateTotal(); // Update overall total
    }

    /**
     * Update total label
     */
    /**
     * Update total label
     */
    private void updateTotal() {
        totalLabel.setText(CartManager.getInstance().getFormattedTotal());
    }

    /**
     * Handle Confirm Order button
     */
    @FXML
    private void handleConfirmOrder() {
        if (CartManager.getInstance().isEmpty()) {
            showAlert("Your cart is empty", Alert.AlertType.WARNING);
            return;
        }
        goToOrderConfirmation();
    }

    /**
     * Handle Continue Shopping button
     */
    @FXML
    private void handleContinueShopping() {
        goToFoodMenu();
    }

    /**
     * Handle back button
     */
    /**
     * Handle back button
     */
    @FXML
    private void handleBack() {
        goBack();
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Shopping Cart");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
