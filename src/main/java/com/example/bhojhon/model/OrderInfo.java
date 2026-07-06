package com.example.bhojhon.model;

public class OrderInfo {
    private String orderId;
    private String passengerName;
    private String phoneNumber;
    private String trainNumber;
    private String seatNumber;
    private String stationName;
    private String orderItems;
    private double totalAmount;
    private String orderDate;

    public OrderInfo(String orderId, String passengerName, String phoneNumber, String trainNumber,
            String seatNumber, String stationName, String orderItems, double totalAmount, String orderDate) {
        this.orderId = orderId;
        this.passengerName = passengerName;
        this.phoneNumber = phoneNumber;
        this.trainNumber = trainNumber;
        this.seatNumber = seatNumber;
        this.stationName = stationName;
        this.orderItems = orderItems;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
    }

    // Getters
    public String getOrderId() {
        return orderId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getStationName() {
        return stationName;
    }

    public String getOrderItems() {
        return orderItems;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getFormattedTotal() {
        return String.format("৳%.0f", totalAmount);
    }
}
