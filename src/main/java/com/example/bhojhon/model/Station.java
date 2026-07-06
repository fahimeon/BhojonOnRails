package com.example.bhojhon.model;

/**
 * Station model class representing a railway station.
 * Demonstrates INHERITANCE by extending BaseEntity.
 */
public class Station extends BaseEntity {
    private String stationCode;
    private String city;

    public Station() {
    }

    public Station(int id, String name, String stationCode, String city) {
        super(id, name);
        this.stationCode = stationCode;
        this.city = city;
    }

    // Getters and Setters
    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Implementation of abstract method from BaseEntity.
     * Demonstrates POLYMORPHISM.
     */
    @Override
    public String getDisplayInfo() {
        return String.format("%s (%s) - %s", getName(), stationCode, city);
    }
}
