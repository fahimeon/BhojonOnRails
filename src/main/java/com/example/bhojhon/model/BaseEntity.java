package com.example.bhojhon.model;


public abstract class BaseEntity {
    // Private fields demonstrate ENCAPSULATION
    private int id;
    private String name;


    public BaseEntity() {
    }


    public BaseEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters (ENCAPSULATION)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public abstract String getDisplayInfo();

    @Override
    public String toString() {
        return name;
    }
}
