package com.MagicalStay.shared.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {

    private String roomNumber;
    private RoomType roomType;
    private RoomCondition roomCondition;
    private Hotel hotel;
    private String description;
    private String features;
    private double price;
    private int capacity;
    private String imagePath;

    public Room(String roomNumber, Hotel hotel, RoomType roomType,
                RoomCondition roomCondition, double price, int capacity,
                String features, String description, String imagePath) {
        this.roomNumber = roomNumber;
        this.hotel = hotel;
        this.roomType = roomType;
        this.roomCondition = roomCondition;
        this.price = price;
        this.capacity = capacity;
        this.features = features;
        this.description = description;
        this.imagePath = imagePath;
    }

    public Room(String roomNumber, RoomType roomType, RoomCondition roomCondition, double price, int capacity, Hotel hotel) {
        this.roomNumber = roomNumber;
        this.hotel = hotel;
        this.roomType = roomType;
        this.roomCondition = roomCondition;
        this.price = price;
        this.capacity = capacity;
    }

    // Constructor adicional por conveniencia
    public Room(String roomNumber, RoomType roomType, RoomCondition roomCondition, Hotel hotel) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomCondition = roomCondition;
        this.hotel = hotel;
    }

    // Constructor por defecto para Jackson
    public Room() {}

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }


    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // toString solo para debugging
    @Override
    public String toString() {
        return "Room{" +
                "roomNumber='" + roomNumber + '\'' +
                ", roomType=" + roomType +
                ", roomCondition=" + roomCondition +
                ", hotelId=" + (hotel != null ? hotel.getHotelId() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Room other = (Room) obj;
        return roomNumber != null && roomNumber.equals(other.roomNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomNumber);
    }
}

