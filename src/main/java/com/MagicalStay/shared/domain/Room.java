package com.MagicalStay.shared.domain;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

public class Room {
    private String roomNumber;
    private RoomType roomType;
    private RoomCondition roomCondition;
    @JsonBackReference
    private Hotel hotel;
    private String description;
    private String features;
    private double price;
    private int capacity;
    private List<String> images;


    public Room(String roomNumber, Hotel hotel, RoomType roomType,
                RoomCondition roomCondition, double price, int capacity,
                String features, String description) {

        this.roomNumber = roomNumber;
        this.hotel = hotel;
        this.roomType = roomType;
        this.roomCondition = roomCondition;
        this.price = price;
        this.capacity = capacity;
        this.features = features;
        this.description = description;
        
    }
    

    public Room(String roomNumber, RoomType roomType, RoomCondition roomCondition, double price, int capacity, Hotel hotel){
        this.roomNumber = roomNumber;
        this.hotel = hotel;
        this.roomType = roomType;
        this.roomCondition = roomCondition;
        this.price = price;
        this.capacity = capacity;
    }

    public Room(String roomNumber, RoomType roomType, RoomCondition roomCondition, Hotel hotel) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomCondition = roomCondition;
        this.hotel = hotel;
    }

    public Room(){
        
    }

    public Room(String roomNumber2, RoomType roomType2, RoomCondition roomCondition2, Hotel hotel2, double price2,
            int capacity2, String features2, String description2) {
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

    

    public void setPrice(double price) {
        this.price = price;
    }

   

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    

    public void setFeatures(String features) {
        this.features = features;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        return "Room{" +
                "roomNumber='" + roomNumber + '\'' +
                ", roomType=" + roomType +
                ", roomCondition=" + roomCondition +
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
