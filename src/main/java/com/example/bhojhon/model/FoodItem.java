package com.example.bhojhon.model;


public class FoodItem extends BaseEntity {
    private int restaurantId;
    private double price; // Price in BDT (Bangladeshi Taka)
    private String category;
    private String description;


    private String imageUrl;


    public FoodItem() {
    }


    public FoodItem(int id, String name, int restaurantId, double price, String category, String description, String imageUrl) {
        super(id, name);
        this.restaurantId = restaurantId;
        this.price = price;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Image URL getter/setter
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public String getFormattedPrice() {
        return String.format("৳%.0f", price);
    }


    @Override
    public String getDisplayInfo() {
        return String.format("%s - %s\n%s\nPrice: ৳%.0f",
                getName(), category, description, price);
    }
}
