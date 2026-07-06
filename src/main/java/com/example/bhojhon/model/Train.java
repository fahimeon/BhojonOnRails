package com.example.bhojhon.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Train model class representing a railway train.
 * Demonstrates INHERITANCE by extending BaseEntity.
 */
public class Train extends BaseEntity {
    private String trainNumber;
    private String route;
    private List<Station> stations;

    public Train() {
        this.stations = new ArrayList<>();
    }

    public Train(int id, String name, String trainNumber, String route) {
        super(id, name);
        this.trainNumber = trainNumber;
        this.route = route;
        this.stations = new ArrayList<>();
    }

    // Getters and Setters
    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public void addStation(Station station) {
        this.stations.add(station);
    }

    /**
     * Implementation of abstract method from BaseEntity.
     * Demonstrates POLYMORPHISM - each entity provides its own implementation.
     */
    @Override
    public String getDisplayInfo() {
        return String.format("%s - %s\nRoute: %s\nStations: %d",
                trainNumber, getName(), route, stations.size());
    }
}
