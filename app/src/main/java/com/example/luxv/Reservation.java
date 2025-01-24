package com.example.luxv;

public class Reservation {
    private String email;
    private Integer roomID;
    private String checkInDate;
    private String checkOutDate;
    private double totalPrice;
    private String status;

    public Reservation(String email, Integer roomID, String checkInDate, String checkOutDate, double totalPrice) {
        this.email = email;
        this.roomID = roomID;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
    }

    // Getters
    public String getEmail() { return email; }
    public Integer getRoomID() { return roomID; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
