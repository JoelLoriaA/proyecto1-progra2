package com.MagicalStay.shared.domain;

public class RoomDTO {
    private String roomNumber;
    private String roomType;
    private String roomCondition;
    private double price;
    private int capacity;
    private String features;
    private String description;
    private String hotelName;
    private String imagePath;

    public RoomDTO(Room room) {
        this.roomNumber = room.getRoomNumber();
        this.roomType = room.getRoomType().name();
        this.roomCondition = room.getRoomCondition().name();
        this.price = room.getPrice();
        this.capacity = room.getCapacity();
        this.features = room.getFeatures();
        this.description = room.getDescription();
        this.hotelName = room.getHotel() != null ? room.getHotel().getName() : null;
        this.imagePath = room.getImagePath();
    }

    public RoomDTO() {}

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomCondition() {
        return roomCondition;
    }

    public void setRoomCondition(String roomCondition) {
        this.roomCondition = roomCondition;
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

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath() {
        this.imagePath = imagePath;
    }
}

