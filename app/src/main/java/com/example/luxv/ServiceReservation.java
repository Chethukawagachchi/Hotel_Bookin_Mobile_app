package com.example.luxv;

public class ServiceReservation {
    private int reservationID;
    private String email;
    private int serviceID;
    private String reservationDate;
    private String reservationTime;
    private double price;

    public ServiceReservation(String email, int serviceID, String reservationDate, String reservationTime, double price) {
        this.email = email;
        this.serviceID = serviceID;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.price = price;
    }

    public int getReservationID() {
        return reservationID;
    }

    public void setReservationID(int reservationID) {
        this.reservationID = reservationID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getServiceID() {
        return serviceID;
    }

    public void setServiceID(int serviceID) {
        this.serviceID = serviceID;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(String reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(String reservationTime) {
        this.reservationTime = reservationTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }


}
