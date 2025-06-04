package com.MagicalStay.shared.domain;

import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
public class Booking {
    private int bookingId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate leavingDate;
    private List<Room> reservedRooms;
    private FrontDeskClerk frontDeskClerk;
    private Guest guest;
    private Hotel hotel;

    @JsonCreator
    public Booking(
            @JsonProperty("bookingId") int bookingId,
            @JsonProperty("startDate") LocalDate startDate,
            @JsonProperty("leavingDate") LocalDate leavingDate,
            @JsonProperty("reservedRooms") List<Room> reservedRooms) {
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

    public FrontDeskClerk getFrontDeskClerk() {
        return frontDeskClerk;
    }

    public void setFrontDeskClerk(FrontDeskClerk frontDeskClerk) {
        this.frontDeskClerk = frontDeskClerk;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
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
