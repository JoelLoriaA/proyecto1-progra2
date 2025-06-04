package com.MagicalStay.shared.data;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.MagicalStay.shared.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.io.RandomAccessFile;



public class RoomDataTest {

    private File hotelFile;
    private File roomFile;
    private HotelData hotelData;
    private RoomData roomData;

    @BeforeEach
    public void setup() throws IOException {
        hotelFile = File.createTempFile("hotels", ".dat");
        roomFile = File.createTempFile("rooms", ".dat");

        hotelData = new HotelData(hotelFile.getAbsolutePath());
        roomData = new RoomData(roomFile.getAbsolutePath(), hotelData);

        Hotel hotel = new Hotel(1, "Hotel Prueba", "norte");
        String result = hotelData.create(hotel);
        System.out.println("Resultado de creación del hotel: " + result);

        if (hotelData.getAllHotels().isEmpty()) {
            throw new IllegalStateException("No se guardó ningún hotel");
        }
    }


    @AfterEach
    public void cleanup() throws IOException {
        if (roomData != null) roomData.close();
        if (hotelData != null) hotelData.close();
    }

    @Test
    public void testCreateRoom() throws IOException {
        System.out.println(hotelData.getAllHotels());
        Hotel storedHotel = hotelData.getAllHotels().stream()
            .filter(h -> h.getHotelId() == 1)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Hotel no encontrado"));


        String roomPath = "data/images/1.jpeg"; 
        Room room = new Room("BBB", storedHotel, RoomType.ESTANDAR, RoomCondition.DISPONIBLE, 150.0, 2, "WIfi",  "SW", roomPath);

        String createResultRoom = roomData.create(room);
        System.out.println("Room creada: " + createResultRoom);
        assertNotNull(createResultRoom, "Resultado de creación nulo");
        assertTrue(createResultRoom.contains("\"success\":true"), "Creación fallida: " + createResultRoom);

        String readResult = roomData.read("BBB");
        assertNotNull(readResult, "Resultado de lectura nulo");
        assertTrue(readResult.contains("BBB"), "No contiene número de habitación");
        assertTrue(readResult.contains("ESTANDAR"), "No contiene tipo de habitación");
        assertTrue(readResult.contains("DISPONIBLE"), "No contiene condición");
        assertTrue(readResult.contains("Hotel Prueba"), "No contiene nombre de hotel");
    }

    @Test
    public void testDeleteRoom() throws IOException {
        Hotel storedHotel = hotelData.getAllHotels().stream()
            .filter(h -> h.getHotelId() == 1)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Hotel no encontrado"));

        String roomPath = "data/images/1.jpeg";     
        Room room = new Room("CCC", storedHotel, RoomType.SUITE, RoomCondition.OCUPADA, 150.0, 2, "WIfi",  "SW", roomPath);

        String createResult = roomData.create(room);
        System.out.println("✅ Creación: " + createResult);

        System.out.println("❗Resultado de creación: " + createResult);

        assertTrue(createResult.contains("\"success\":true"), "❌ Creación fallida");

        String readBeforeDelete = roomData.read("CCC");
        System.out.println("📖 Antes de eliminar: " + readBeforeDelete);
        assertTrue(readBeforeDelete.contains("CCC"), "❌ La habitación no fue creada correctamente");

        String deleteResult = roomData.delete("CCC");
        System.out.println("🗑 Resultado de eliminación: " + deleteResult);
        assertTrue(deleteResult.contains("\"success\":true"), "❌ Eliminación fallida");

        String readAfterDelete = roomData.read("CCC");
        System.out.println("📖 Después de eliminar: " + readAfterDelete);
        assertTrue(readAfterDelete.contains("no encontrada") || readAfterDelete.contains("\"success\":false"),
                "❌ La habitación todavía existe después de eliminarla");
                
    }

    @Test
    public void testDeleteOneRoomOnly() throws IOException {
        Hotel storedHotel = hotelData.getAllHotels().stream()
            .filter(h -> h.getHotelId() == 1)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Hotel no encontrado"));

        // Crear tres habitaciones distintas
        Room room1 = new Room("101", RoomType.ESTANDAR, RoomCondition.DISPONIBLE, storedHotel);
        Room room2 = new Room("102", RoomType.SUITE, RoomCondition.OCUPADA, storedHotel);
        Room room3 = new Room("103", RoomType.SUITE, RoomCondition.DISPONIBLE, storedHotel);

        roomData.create(room1);
        roomData.create(room2);
        roomData.create(room3);

        // Eliminar solo una habitación

        System.out.println(">>> Habitaciones antes de borrar:");
        for (Room room : roomData.loadRooms()) {
            System.out.println(" - " + room.getRoomNumber().trim() + " (HotelID: " + room.getHotel().getHotelId() + ")");
        }

        String deleteResult = roomData.delete("102");
        System.out.println("Resultado eliminación: " + deleteResult);

        assertTrue(deleteResult.contains("\"success\":true"), "Falló al eliminar habitación 102");

        // Leer todas las habitaciones restantes
        String jsonResponse = roomData.readAll();
        JsonResponse response = new ObjectMapper().readValue(jsonResponse, JsonResponse.class);
        List<Room> roomsRemaining = new ObjectMapper().convertValue(response.getData(), new TypeReference<List<Room>>() {});

        // Verificaciones
        assertEquals(2, roomsRemaining.size(), "Deben quedar 2 habitaciones");
        assertTrue(roomsRemaining.stream().anyMatch(r -> r.getRoomNumber().equals("101")), "Falta la habitación 101");
        assertTrue(roomsRemaining.stream().anyMatch(r -> r.getRoomNumber().equals("103")), "Falta la habitación 103");
        assertFalse(roomsRemaining.stream().anyMatch(r -> r.getRoomNumber().equals("102")), "La habitación 102 no fue eliminada correctamente");
    }

    @Test
    public void testCreateMultipleRooms_afterWipe() throws IOException {
        roomData.clearDataFile();    

        // Obtener hotel existente
        Hotel storedHotel = hotelData.getAllHotels().stream()
            .filter(h -> h.getHotelId() == 1)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Hotel con ID 1 no encontrado"));

        // Lista de habitaciones a crear
        String roomPath = "data/images/1.jpeg"; 
        List<Room> roomsToCreate = List.of(
            new Room("101", storedHotel, RoomType.ESTANDAR, RoomCondition.DISPONIBLE, 100.0, 2, "WiFi,TV", "Cerca del lobby", roomPath),
            new Room("102", storedHotel, RoomType.SUITE, RoomCondition.OCUPADA, 200.0, 4, "Jacuzzi,WiFi", "Vista al mar", roomPath),
            new Room("103", storedHotel, RoomType.SUITE, RoomCondition.OCUPADA, 150.0, 3, "WiFi", "Segundo piso", roomPath),
            new Room("104", storedHotel, RoomType.ESTANDAR, RoomCondition.EN_MANTENIMIENTO, 120.0, 2, "A/C,TV", "Cerca de la salida", roomPath)
        );

        // Crear habitaciones
        for (Room room : roomsToCreate) {
            String result = roomData.create(room);
            System.out.println("Creación habitación " + room.getRoomNumber() + ": " + result);
            assertNotNull(result, "Resultado de creación nulo para " + room.getRoomNumber());
            assertTrue(result.contains("\"success\":true"), "Creación fallida para " + room.getRoomNumber());
        }

        // Verificar lectura de todas las habitaciones
        for (Room room : roomsToCreate) {
            String readResult = roomData.read(room.getRoomNumber());
            System.out.println("Lectura habitación " + room.getRoomNumber() + ": " + readResult);
            assertNotNull(readResult, "Lectura nula para " + room.getRoomNumber());
            assertTrue(readResult.contains(room.getRoomNumber()), "No contiene número de habitación");
            assertTrue(readResult.contains(room.getRoomType().name()), "No contiene tipo");
            assertTrue(readResult.contains(room.getRoomCondition().name()), "No contiene condición");
        }
    }

}