package com.example.bhojhon.util;

import com.example.bhojhon.model.RestaurantOwner;

/**
 * Singleton class to manage restaurant owner session
 */
public class RestaurantSession {
    private static RestaurantSession instance;
    private RestaurantOwner currentOwner;

    private RestaurantSession() {
    }

    public static RestaurantSession getInstance() {
        if (instance == null) {
            instance = new RestaurantSession();
        }
        return instance;
    }

    public void setCurrentOwner(RestaurantOwner owner) {
        this.currentOwner = owner;
    }

    public RestaurantOwner getCurrentOwner() {
        return currentOwner;
    }

    public void clearSession() {
        this.currentOwner = null;
    }

    public boolean isLoggedIn() {
        return currentOwner != null;
    }
}
