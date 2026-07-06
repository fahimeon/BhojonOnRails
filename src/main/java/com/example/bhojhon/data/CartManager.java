package com.example.bhojhon.data;

import com.example.bhojhon.model.CartItem;
import com.example.bhojhon.model.FoodItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Cart management class using Singleton pattern.
 * Manages shopping cart state throughout the application.
 */
public class CartManager {
    private static CartManager instance;

    // Observable list for automatic UI updates
    private ObservableList<CartItem> cartItems;

    // Context data for order
    private String selectedTrainNumber;
    private String selectedStationName;

    // Passenger info
    private String passengerName;
    private String phoneNumber;
    private String seatNumber;
    private String deliveryNote;
    private String pnr;
    private String journeyDate;
    private String userEmail;

    /**
     * Private constructor for Singleton pattern
     */
    private CartManager() {
        cartItems = FXCollections.observableArrayList();
    }

    /**
     * Get singleton instance
     */
    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    /**
     * Add item to cart or increase quantity if already exists
     */
    public void addItem(FoodItem foodItem, int quantity) {
        // Check if item already in cart
        CartItem existingItem = cartItems.stream()
                .filter(item -> item.getFoodItem().getId() == foodItem.getId())
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Increase quantity
            int newQty = existingItem.getQuantity() + quantity;
            if (newQty <= 0) {
                removeItem(existingItem);
            } else {
                existingItem.setQuantity(newQty);
            }
        } else {
            // Add new item if quantity > 0
            if (quantity > 0) {
                cartItems.add(new CartItem(foodItem, quantity));
            }
        }
    }

    /**
     * Remove item from cart
     */
    public void removeItem(CartItem cartItem) {
        cartItems.remove(cartItem);
    }

    /**
     * Update item quantity
     */
    public void updateQuantity(CartItem cartItem, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(cartItem);
        } else {
            cartItem.setQuantity(newQuantity);
        }
    }

    /**
     * Clear all items from cart
     */
    public void clearCart() {
        cartItems.clear();
        selectedTrainNumber = null;
        selectedStationName = null;
        passengerName = null;
        phoneNumber = null;
        seatNumber = null;
        deliveryNote = null;
        pnr = null;
        journeyDate = null;
        userEmail = null;
    }

    public void setPassengerDetails(String name, String phone, String seat, String note, String pnr,
            String journeyDate, String email) {
        this.passengerName = name;
        this.phoneNumber = phone;
        this.seatNumber = seat;
        this.deliveryNote = note;
        this.pnr = pnr;
        this.journeyDate = journeyDate;
        this.userEmail = email;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getDeliveryNote() {
        return deliveryNote;
    }

    public String getPnr() {
        return pnr;
    }

    public String getJourneyDate() {
        return journeyDate;
    }

    /**
     * Get cart items as observable list
     */
    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }

    /**
     * Calculate total cart value
     */
    public double getTotal() {
        return cartItems.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    /**
     * Get formatted total with BDT currency
     */
    public String getFormattedTotal() {
        return String.format("৳%.0f", getTotal());
    }

    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    /**
     * Get number of items in cart
     */
    public int getItemCount() {
        return cartItems.size();
    }

    /**
     * Get quantity of specific food item in cart
     */
    public int getQuantity(FoodItem foodItem) {
        return cartItems.stream()
                .filter(item -> item.getFoodItem().getId() == foodItem.getId())
                .findFirst()
                .map(CartItem::getQuantity)
                .orElse(0);
    }

    // Context getters and setters
    public String getSelectedTrainNumber() {
        return selectedTrainNumber;
    }

    public void setSelectedTrainNumber(String selectedTrainNumber) {
        this.selectedTrainNumber = selectedTrainNumber;
    }

    public String getSelectedStationName() {
        return selectedStationName;
    }

    public void setSelectedStationName(String selectedStationName) {
        this.selectedStationName = selectedStationName;
    }
}
