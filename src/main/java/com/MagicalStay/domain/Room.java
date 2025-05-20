package com.MagicalStay.domain;

public class Room {
    private String roomNumber;
    private RoomType roomType;
    private RoomCondition roomCondition;
    private Hotel hotel;

    public Room(String roomNumber, RoomType roomType, RoomCondition roomCondition, Hotel hotel) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomCondition = roomCondition;
        this.hotel = hotel;
    }

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

    // Método toString (opcional, útil para imprimir la habitación)
    @Override
    public String toString() {
        return "Room{" +
                "roomNumber='" + roomNumber + '\'' +
                ", roomType=" + roomType +
                ", roomCondition=" + roomCondition +
                '}';
    }

}
