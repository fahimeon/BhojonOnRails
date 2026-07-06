package com.example.bhojhon.model;

/**
 * Restaurant model class representing a food establishment near a station.
 * Demonstrates INHERITANCE by extending BaseEntity.
 */
public class Restaurant extends BaseEntity {
    private int stationId;
    private String cuisine;
    private double rating;

    public Restaurant() {
    }

    public Restaurant(int id, String name, int stationId, String cuisine, double rating) {
        super(id, name);
        this.stationId = stationId;
        this.cuisine = cuisine;
        this.rating = rating;
    }

    // Getters and Setters
    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    /**
     * Implementation of abstract method from BaseEntity.
     * Demonstrates POLYMORPHISM.
     */
    @Override
    public String getDisplayInfo() {
        return String.format("%s\nCuisine: %s | Rating: %.1f⭐",
                getName(), cuisine, rating);
    }
}
