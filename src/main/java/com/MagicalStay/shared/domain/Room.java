package com.MagicalStay.shared.domain;

import java.util.List;

public class Room {
    private String roomNumber;
    private RoomType roomType;
    private RoomCondition roomCondition;
    private Hotel hotel;
    private String description;
    private String features;
    private double price;
    private int capacity;
    private List<String> images;


    public Room(String roomNumber, Hotel hotel, RoomType roomType,
                RoomCondition roomCondition, double price, int capacity,
                String features, String description, List<String> images) {

        this.roomNumber = roomNumber;
        this.hotel = hotel;
        this.roomType = roomType;
        this.roomCondition = roomCondition;
        this.price = price;
        this.capacity = capacity;
        this.features = features;
        this.description = description;
        this.images = images;
    }

    public Room(String roomNumber, RoomType roomType, RoomCondition roomCondition, Hotel hotel) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomCondition = roomCondition;
        this.hotel = hotel;
    }

    public Room(String string, String string2, int i, boolean b) {
        //TODO Auto-generated constructor stub
    }

    // Agregar getters y setters para los nuevos campos
    public String getDescription() { return description; }
    public String getFeatures() { return features; }
    public double getPrice() { return price; }
    public int getCapacity() { return capacity; }
    public List<String> getImages() { return images; }

    // Getters y setters
    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public RoomCondition getRoomCondition() {
        return roomCondition;
    }

    public void setRoomCondition(RoomCondition roomCondition) {
        this.roomCondition = roomCondition;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber='" + roomNumber + '\'' +
                ", roomType=" + roomType +
                ", roomCondition=" + roomCondition +
                '}';
    }

}
