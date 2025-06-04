package com.MagicalStay;

import static org.junit.jupiter.api.Assertions.*;

import com.MagicalStay.client.ui.controllers.RoomManagementController;
import com.MagicalStay.shared.data.HotelData;
import com.MagicalStay.shared.data.RoomData;
import com.MagicalStay.shared.domain.Hotel;
import com.MagicalStay.shared.domain.Room;
import com.MagicalStay.shared.domain.RoomCondition;
import com.MagicalStay.shared.domain.RoomType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class RoomManagementControllerTest {

    private File hotelFile;
    private File roomFile;

    private HotelData hotelData;
    private RoomData roomData;

    private RoomManagementController controller;

    // Inicializar JavaFX Toolkit solo una vez
    @BeforeAll
    public static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit ya está inicializado
        }
    }

    @BeforeEach
    public void setup() throws IOException {
        // Archivos temporales
        hotelFile = File.createTempFile("hotels", ".dat");
        roomFile = File.createTempFile("rooms", ".dat");

        hotelData = new HotelData(hotelFile.getAbsolutePath());
        roomData = new RoomData(roomFile.getAbsolutePath(), hotelData);

        controller = new RoomManagementController();

        // Inyectar dependencias reales
        controller.roomData = roomData;
        controller.objectMapper = new ObjectMapper();
        controller.roomList = FXCollections.observableArrayList();
        controller.statusLabel = new Label();

        // Crear hotel y setearlo como seleccionado
        Hotel hotel = new Hotel(1, "Hotel Test", "centro");
        hotelData.create(hotel);
        // Fuerza guardar el hotel en archivo y recargarlo correctamente
        hotelData.close();
        hotelData = new HotelData(hotelFile.getAbsolutePath());
        Hotel hotelFromFile = hotelData.findById(1);
        assertNotNull(hotelFromFile, "Hotel no se recuperó desde archivo");

        controller.selectedHotel = hotelFromFile; 

        
    }

    @AfterEach
    public void cleanup() throws IOException {
        if (roomData != null) roomData.close();
        if (hotelData != null) hotelData.close();
    }

    @Test
public void testLoadRoomsFromFile() throws Exception {
    // Limpiar archivo de habitaciones
    roomData.clearDataFile();

    // Crear y guardar hotel
    Hotel hotel = new Hotel(1, "Hotel Test", "Centro");
    String resultHotel = hotelData.create(hotel);
    assertTrue(resultHotel.contains("\"success\":true"), "Hotel no se creó correctamente");

    // Refrescar instancia de hotel desde archivo (simula reinicio de sistema)
    hotelData.close();
    hotelData = new HotelData(hotelFile.getAbsolutePath());
    Hotel storedHotel = hotelData.findById(1);
    assertNotNull(storedHotel, "No se recuperó el hotel desde archivo");

    // Crear roomData con hotelData actualizado
    roomData.close();
    roomData = new RoomData(roomFile.getAbsolutePath(), hotelData);

    // Crear habitación asociada al hotel recuperado
    String roomPath = "data/images/1.jpeg"; 
    Room room = new Room("101", storedHotel, RoomType.ESTANDAR, RoomCondition.DISPONIBLE, 100.0, 2, "WiFi,TV", "Cerca del lobby", roomPath);
    String roomResult = roomData.create(room);
    System.out.println("Resultado de crear habitación: " + roomResult);
    assertTrue(roomResult.contains("\"success\":true"), "No se pudo crear la habitación");

    // Reabrir para lectura
    roomData.close();
    roomData = new RoomData(roomFile.getAbsolutePath(), hotelData);

    // Configurar controller
    RoomManagementController controller = new RoomManagementController();
    controller.roomData = roomData;
    controller.selectedHotel = storedHotel;
    controller.roomList = FXCollections.observableArrayList();
    controller.statusLabel = new Label();
    controller.objectMapper = new ObjectMapper();

    // Debug directo desde archivo
    System.out.println("Lectura directa del archivo con roomData:");
    List<Room> directRooms = roomData.loadRooms();
    for (Room r : directRooms) {
        System.out.println("Room: " + r.getRoomNumber() + ", hotelId: " + 
            (r.getHotel() != null ? r.getHotel().getHotelId() : "null"));
    }
    System.out.println("Total en archivo: " + directRooms.size());

    // Ejecutar carga en controller
    controller.loadRoomsFromFile();

    // Debug de resultado cargado en controller
    System.out.println("Habitaciones cargadas:");
    controller.roomList.forEach(r -> System.out.println(r.getRoomNumber() + " - HotelID: " + r.getHotel().getHotelId()));

    // Verificaciones
    assertEquals(1, controller.roomList.size(), "Debe haber una habitación cargada");
    Room loadedRoom = controller.roomList.get(0);
    assertEquals("101", loadedRoom.getRoomNumber(), "Número de habitación incorrecto");
    assertEquals(storedHotel.getHotelId(), loadedRoom.getHotel().getHotelId(), "Hotel incorrecto");
}


@Test
void testHandleSaveCreatesRoomSuccessfully() throws IOException {

    roomData.clearDataFile();
    // Crear archivo temporal de hoteles y habitaciones
    File hotelFile = File.createTempFile("test_hotels", ".dat");
    File roomFile = File.createTempFile("test_rooms", ".dat");
    hotelFile.deleteOnExit();
    roomFile.deleteOnExit();

    // Crear datos de prueba
    Hotel hotel = new Hotel(1, "Hotel Test", "Centro");
    HotelData hotelData = new HotelData(hotelFile.getAbsolutePath());
    hotelData.create(hotel);

    RoomData roomData = new RoomData(roomFile.getAbsolutePath(), hotelData);

    RoomManagementController controller = new RoomManagementController();

    controller.roomData = roomData;
    controller.selectedHotel = hotel;
    controller.editMode = false; 
    controller.numberTextField = new TextField("101");
    controller.priceTextField = new TextField("150.0");
    controller.typeComboBox = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
    controller.typeComboBox.setValue(RoomType.ESTANDAR);
    controller.statusComboBox = new ComboBox<>(FXCollections.observableArrayList(RoomCondition.values()));
    controller.statusComboBox.setValue(RoomCondition.DISPONIBLE);
    controller.capacitySpinner = new Spinner<>(1, 10, 2);
    controller.featuresTextArea = new TextArea("WiFi, TV");
    controller.descriptionTextArea = new TextArea("Cerca del lobby");
    controller.selectedImagePath = "images/habitacion101.jpg";

    controller.roomListView = new ListView<>();
    controller.saveButton = new Button();
    controller.cancelButton = new Button();

    controller.roomList = FXCollections.observableArrayList();

    Platform.runLater(() -> {
        controller.handleSave();

        try {
            RoomData roomDataReloaded = new RoomData(roomFile.getAbsolutePath(), hotelData);
            List<Room> rooms = roomDataReloaded.getRoomsByHotelId(hotel.getHotelId());

            assertEquals(1, rooms.size(), "Se esperaba una habitación guardada");

            Room savedRoom = rooms.get(0);
            assertEquals("101", savedRoom.getRoomNumber());
            assertEquals(RoomType.ESTANDAR, savedRoom.getRoomType());
            assertEquals(RoomCondition.DISPONIBLE, savedRoom.getRoomCondition());
            assertEquals(150.0, savedRoom.getPrice());
            assertEquals(2, savedRoom.getCapacity());
            assertEquals("WiFi, TV", savedRoom.getFeatures());
            assertEquals("Cerca del lobby", savedRoom.getDescription());
            assertEquals("images/habitacion101.jpg", savedRoom.getImagePath());

        } catch (IOException e) {
            fail("Error al recargar datos de habitaciones: " + e.getMessage());
        }
    });

    try {
        Thread.sleep(1000); 
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}




}

