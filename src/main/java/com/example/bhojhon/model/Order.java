package com.example.bhojhon.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private List<CartItem> items;
    private double total;
    private String trainNumber;
    private String stationName;
    private String passengerName;
    private String phoneNumber;
    private String seatNumber;
    private String deliveryNote;
    private LocalDateTime orderTime;

    public Order() {
        this.items = new ArrayList<>();
        this.orderTime = LocalDateTime.now();
    }

    public Order(String orderId, List<CartItem> items, String trainNumber, String stationName,
            String passengerName, String phoneNumber, String seatNumber, String deliveryNote) {
        this.orderId = orderId;
        this.items = new ArrayList<>(items);
        this.trainNumber = trainNumber;
        this.stationName = stationName;
        this.passengerName = passengerName;
        this.phoneNumber = phoneNumber;
        this.seatNumber = seatNumber;
        this.deliveryNote = deliveryNote;
        this.orderTime = LocalDateTime.now();
        calculateTotal();
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
        calculateTotal();
    }

    public double getTotal() {
        return total;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
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

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    private void calculateTotal() {
        this.total = items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public String getFormattedTotal() {
        return String.format("৳%.0f", total);
    }

    public String getFormattedOrderTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return orderTime.format(formatter);
    }

    @Override
    public String toString() {
        return String.format("Order #%s - %s - Total: ৳%.0f",
                orderId, stationName, total);
    }
}
