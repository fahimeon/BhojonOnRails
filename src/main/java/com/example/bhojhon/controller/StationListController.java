package com.example.bhojhon.controller;

import com.example.bhojhon.data.CartManager;
import com.example.bhojhon.data.DataManager;
import com.example.bhojhon.exception.StationSelectionException;
import com.example.bhojhon.model.Station;
import com.example.bhojhon.model.Train;
import com.example.bhojhon.util.BaseController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;

/**
 * Controller for the Station List screen.
 * Displays stations of the selected train.
 */
public class StationListController extends BaseController {

    @FXML
    private Label trainInfoLabel;

    @FXML
    private ListView<Station> stationListView;

    private Train selectedTrain;

    @Override
    public void initialize() {
        // Get selected train from navigation manager
        selectedTrain = (Train) navigationManager.getData("selectedTrain");

        if (selectedTrain != null) {
            trainInfoLabel.setText(selectedTrain.getName() + " - " + selectedTrain.getRoute());

            // Populate station list
            stationListView.getItems().addAll(selectedTrain.getStations());

            // Set custom cell factory to display station info inside square rounded boxes
            stationListView.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Station station, boolean empty) {
                    super.updateItem(station, empty);
                    if (empty || station == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(null);
                        javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(station.getDisplayInfo());
                        javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(nameLabel);
                        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        box.getStyleClass().add("station-box");
                        setGraphic(box);
                    }
                }
            });

            // Add single-click listener
            stationListView.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    handleSelectStation();
                }
            });
        }
    }

    /**
     * Handle station selection
     */
    @FXML
    private void handleSelectStation() {
        try {
            Station selectedStation = validateSelectedStation();
            validateRestaurantAvailability(selectedStation);

            CartManager.getInstance().setSelectedStationName(selectedStation.getName());
            navigationManager.putData("selectedStation", selectedStation);
            goToRestaurantList();
        } catch (StationSelectionException e) {
            showAlert(e.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception e) {
            showAlert("Unexpected error while selecting station: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Station validateSelectedStation() throws StationSelectionException {
        Station selectedStation = stationListView.getSelectionModel().getSelectedItem();

        if (selectedStation == null) {
            throw new StationSelectionException("Please select a station from the list first.");
        }

        return selectedStation;
    }

    private void validateRestaurantAvailability(Station selectedStation) throws StationSelectionException {
        var restaurants = DataManager.getInstance().getRestaurantsByStationId(selectedStation.getId());

        if (restaurants.isEmpty()) {
            throw new StationSelectionException("No restaurants registered at " + selectedStation.getName());
        }
    }

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
        alert.setTitle("Station Selection");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
