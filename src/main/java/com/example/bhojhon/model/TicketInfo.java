package com.example.bhojhon.model;

/**
 * Model class to hold extracted ticket information.
 */
public class TicketInfo {
    private String passengerName;
    private String phoneNumber;
    private String pnr; // Ticket Number
    private String trainNumber;
    private String seatNumber;
    private String journeyDate;
    private String deliveryNote;

    public TicketInfo() {
    }

    public TicketInfo(String passengerName, String phoneNumber, String pnr, String trainNumber, String seatNumber,
            String journeyDate, String deliveryNote) {
        this.passengerName = passengerName;
        this.phoneNumber = phoneNumber;
        this.pnr = pnr;
        this.trainNumber = trainNumber;
        this.seatNumber = seatNumber;
        this.journeyDate = journeyDate;
        this.deliveryNote = deliveryNote;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(String journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getDeliveryNote() {
        return deliveryNote;
    }

    public void setDeliveryNote(String deliveryNote) {
        this.deliveryNote = deliveryNote;
    }

    @Override
    public String toString() {
        return "TicketInfo{" +
                "passengerName='" + passengerName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", pnr='" + pnr + '\'' +
                ", trainNumber='" + trainNumber + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", journeyDate='" + journeyDate + '\'' +
                ", deliveryNote='" + deliveryNote + '\'' +
                '}';
    }
}
