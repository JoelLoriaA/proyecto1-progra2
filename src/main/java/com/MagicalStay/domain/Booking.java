package com.MagicalStay.domain;

import java.time.LocalDate;
import java.util.List;

public class Booking {
    private int bookingId;
    private LocalDate startDate;
    private LocalDate leavingDate;
    private List<Room> reservedRooms;

    public Booking(int bookingId, LocalDate startDate, LocalDate leavingDate, List<Room> reservedRooms) {
        this.bookingId = bookingId;
        this.startDate = startDate;
        this.leavingDate = leavingDate;
        this.reservedRooms = reservedRooms;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getLeavingDate() {
        return leavingDate;
    }

    public void setLeavingDate(LocalDate leavingDate) {
        this.leavingDate = leavingDate;
    }

    public List<Room> getReservedRooms() {
        return reservedRooms;
    }

    public void setReservedRooms(List<Room> reservedRooms) {
        this.reservedRooms = reservedRooms;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", startDate=" + startDate +
                ", leavingDate=" + leavingDate +
                ", reservedRooms=" + reservedRooms +
                '}';
    }


}
