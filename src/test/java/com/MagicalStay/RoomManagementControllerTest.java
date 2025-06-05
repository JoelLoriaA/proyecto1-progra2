package com.MagicalStay;

import static org.junit.jupiter.api.Assertions.*;

import com.MagicalStay.client.ui.controllers.RoomManagementController;
import com.MagicalStay.shared.data.HotelData;
import com.MagicalStay.shared.data.JsonResponse;
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
    private final ObjectMapper objectMapper = new ObjectMapper();
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
        hotelFile = File.createTempFile("hotels", ".dat");
        roomFile = File.createTempFile("rooms", ".dat");

        // Inicializar hotelData primero
        hotelData = new HotelData(hotelFile.getAbsolutePath());

        // Crear y guardar hotel
        Hotel hotel = new Hotel(1, "Hotel Test", "Centro");
        String hotelResult = hotelData.create(hotel);
        assertTrue(hotelResult.contains("\"success\":true"), "❌ No se creó el hotel correctamente");

        // Cerrar y reabrir hotelData antes de pasarlo a RoomData
        hotelData.close();
        hotelData = new HotelData(hotelFile.getAbsolutePath());

        // ✅ Ahora inicializar roomData con hotelData ya listo
        roomData = new RoomData(roomFile.getAbsolutePath(), hotelData);

        // Configurar controlador
        controller = new RoomManagementController();
        controller.roomData = roomData;
        controller.objectMapper = new ObjectMapper();
        controller.roomList = FXCollections.observableArrayList();
        controller.statusLabel = new Label();

        // Obtener hotel como lo haría RoomData
        String json = hotelData.retrieveById(1);
        System.out.println("📄 retrieveById JSON: " + json);
        JsonResponse response = objectMapper.readValue(json, JsonResponse.class);
        assertTrue(response.isSuccess(), "❌ Hotel no recuperado exitosamente");

        Hotel hotelFromFile = objectMapper.convertValue(response.getData(), Hotel.class);
        assertNotNull(hotelFromFile, "❌ Hotel no se recuperó desde archivo");

        controller.selectedHotel = hotelFromFile;
    }

    

    @AfterEach
    public void cleanup() throws IOException {
        if (roomData != null) roomData.close();
        if (hotelData != null) hotelData.close();
    }

    
    @Test
    public void testLoadRoomsFromFile() throws Exception {
        System.out.println("🧪 Iniciando testLoadRoomsFromFile");

        // Limpiar archivo de habitaciones
        roomData.clearDataFile();
        System.out.println("✅ Archivo de habitaciones limpiado");

        // Crear y guardar hotel
        Hotel hotel = new Hotel(1, "Hotel Test", "Centro");
        String hotelResult = hotelData.create(hotel);
        System.out.println("✅ Resultado al crear hotel: " + hotelResult);
        assertTrue(hotelResult.contains("\"success\":true"), "❌ Hotel no se creó correctamente");

        // Recuperar hotel desde archivo
        String hotelJson = hotelData.retrieveById(1);
        System.out.println("🔍 Resultado de retrieveById: " + hotelJson);
        JsonResponse response = objectMapper.readValue(hotelJson, JsonResponse.class);
        assertTrue(response.isSuccess(), "❌ Falló retrieveById");

        Hotel selectedHotel = objectMapper.convertValue(response.getData(), Hotel.class);
        System.out.println("✅ Hotel recuperado: " + selectedHotel);
        assertNotNull(selectedHotel, "❌ Hotel no recuperado correctamente");

        // Crear habitación con hotel recuperado
        Room room = new Room("101", selectedHotel, RoomType.ESTANDAR, RoomCondition.DISPONIBLE,
                            100.0, 2, "WiFi,TV", "Cerca del lobby", "data/images/1.jpeg");

        String roomResult = roomData.create(room);  // ✅ Importante: antes de cerrar roomData
        System.out.println("✅ Resultado al crear habitación: " + roomResult);
        assertTrue(roomResult.contains("\"success\":true"), "❌ Falló al crear habitación");

        // Leer y mostrar habitaciones en archivo (opcional pero útil para debug)
        String allRoomsJson = roomData.readAll();
        System.out.println("📄 Habitaciones en archivo tras creación:");
        System.out.println(allRoomsJson);

        // ✅ Cerrar roomData SOLO DESPUÉS de haber creado y leído todo
        roomData.close();

        // Reabrir para simular flujo real
        roomData = new RoomData(roomFile.getAbsolutePath(), hotelData);
        System.out.println("🔁 RoomData reabierto desde archivo");

        // Configurar controlador
        RoomManagementController controller = new RoomManagementController();
        controller.roomData = roomData;
        controller.selectedHotel = selectedHotel;
        controller.roomList = FXCollections.observableArrayList();
        controller.statusLabel = new Label();
        controller.objectMapper = new ObjectMapper();

        // Cargar habitaciones
        System.out.println("📥 Cargando habitaciones con loadRoomsFromFile()");
        controller.loadRoomsFromFile();

        System.out.println("📋 Tamaño de roomList: " + controller.roomList.size());
        assertEquals(1, controller.roomList.size(), "❌ Debe haber una habitación cargada");

        Room loadedRoom = controller.roomList.get(0);
        System.out.println("✅ Habitación cargada: " + loadedRoom);

        // Verificar campos
        assertEquals("101", loadedRoom.getRoomNumber(), "❌ Número incorrecto");
        assertEquals(selectedHotel.getHotelId(), loadedRoom.getHotel().getHotelId(), "❌ Hotel incorrecto");

        System.out.println("✅ testLoadRoomsFromFile completado exitosamente");
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

