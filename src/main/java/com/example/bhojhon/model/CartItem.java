package com.example.bhojhon.model;


public class CartItem {
    private FoodItem foodItem;
    private int quantity;

    public CartItem() {
    }

    public CartItem(FoodItem foodItem, int quantity) {
        this.foodItem = foodItem;
        this.quantity = quantity;
    }

    // Getters and Setters
    public FoodItem getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public double getSubtotal() {
        return foodItem.getPrice() * quantity;
    }


    public String getFormattedSubtotal() {
        return String.format("৳%.0f", getSubtotal());
    }

    @Override
    public String toString() {
        return String.format("%s x %d = ৳%.0f",
                foodItem.getName(), quantity, getSubtotal());
    }
}
