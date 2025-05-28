package com.MagicalStay.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.MagicalStay.shared.data.*;
import com.MagicalStay.shared.domain.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;

public class RoomDataTest {

    private static String filePath;
    private RoomData roomData;

    @BeforeAll
    public static void setupPath() {
        String desktopPath = System.getProperty("user.home") + "/Escritorio/data";
        new File(desktopPath).mkdirs();
        filePath = desktopPath + "/rooms_test.dat";
    }

    @BeforeEach
    public void setup() throws IOException {
        HotelData dummyHotelData = new DummyHotelData();
        roomData = new RoomData(filePath, dummyHotelData);
        new File(filePath).delete(); // limpia el archivo antes de cada prueba
    }

    
    @Test
    public void testCreateAndReadRoom() throws IOException {
        Hotel hotel = new Hotel(1L, "Hotel Central", "San José"); // ID como long
        Room room = new Room(
            "101",
            hotel,
            RoomType.SUITE,
            RoomCondition.OCUPADA,
            120.0,
            2,
            "TV, WiFi",
            "Cómoda habitación para dos personas",
            Arrays.asList("img1.jpg", "img2.jpg")
        );

        String result = roomData.create(room);
        assertTrue(result.contains("creada exitosamente"));

        String allRoomsJson = roomData.readAll();
        assertTrue(allRoomsJson.contains("101"));
        assertTrue(allRoomsJson.contains("Hotel Central"));
    }

  

    // Dummy HotelData que siempre devuelve el mismo hotel
    private static class DummyHotelData extends HotelData {
        @Override
        public String read(int id) {
            return "{\"success\": true, \"data\": {\"hotelId\": 1, \"name\": \"Hotel Central\", \"address\": \"San José\"}}";
        }
    }
    
}
