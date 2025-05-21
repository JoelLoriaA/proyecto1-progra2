package com.MagicalStay.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {


    @Test
    void createRoom() {
        List<Room> rooms = new ArrayList<>();
        Hotel hotel = new Hotel(1, "Hotel Paraíso", "Av. Central 123, Ciudad Fantasía", rooms);

        rooms.add(new Room("101", RoomType.ESTANDAR, RoomCondition.DISPONIBLE,hotel));
        rooms.add(new Room("202", RoomType.SUITE, RoomCondition.EN_MANTENIMIENTO,hotel));
    }
}