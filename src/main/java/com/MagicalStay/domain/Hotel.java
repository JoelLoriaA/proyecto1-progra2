package com.MagicalStay.domain;

public class Hotel {
    private int hotelId;
    private String name;
    private String address;
    private Room room;

    public Hotel(int hotelId, String name, String address, Room room) {
        this.hotelId = hotelId;
        this.name = name;
        this.address = address;
        this.room = room;
    }

    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}