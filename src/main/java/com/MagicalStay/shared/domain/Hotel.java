package com.MagicalStay.shared.domain;

import java.util.List;

public class Hotel {
    private int hotelId;
    private String name;
    private String address;
    private List<Room> rooms;  // Lista de habitaciones
    private List<Guest> guests;

    public Hotel(int hotelId, String name, String address, List<Room> rooms) {
        this.hotelId = hotelId;
        this.name = name;
        this.address = address;
        this.rooms = rooms;
    }


    public Hotel(int hotelId, String name, String address, List<Room> rooms, List<Guest> guests) {
        this.hotelId = hotelId;
        this.name = name;
        this.address = address;
        this.rooms = rooms;
        this.guests = guests;
    }

    public Hotel(long string, String string2, String string3) {
        //TODO Auto-generated constructor stub
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

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    @Override
    public String toString() {
        return "Hotel{" +
                "hotelId=" + hotelId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", room=" + rooms +
                '}';
    }
}
