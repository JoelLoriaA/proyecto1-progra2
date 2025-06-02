package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.sockets.SocketCliente;
import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.shared.data.HotelData;
import com.MagicalStay.shared.data.JsonResponse;
import com.MagicalStay.shared.data.RoomData;
import com.MagicalStay.shared.domain.*;
import com.MagicalStay.shared.util.FXUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.Closeable;
import javafx.application.Platform;
import java.util.stream.Collectors;

public class RoomManagementController implements Closeable {
    @FXML private ComboBox<Hotel> hotelComboBox;
    @FXML private TextField searchTextField, numberTextField, priceTextField;
    @FXML private ComboBox<RoomType> typeComboBox;
    @FXML private ComboBox<RoomCondition> statusComboBox;
    @FXML private TextArea descriptionTextArea, featuresTextArea;
    @FXML private Spinner<Integer> capacitySpinner;
    @FXML private Button addButton, editButton, deleteButton, saveButton, cancelButton, searchButton;
    @FXML private Label statusLabel;
    @FXML private TableView<Room> roomTableView;
    @FXML private TableColumn<Room, String> roomNumberColumn, roomTypeColumn, roomStatusColumn;
    @FXML private TableColumn<Room, Integer> roomCapacityColumn;
    @FXML private TableColumn<Room, Double> roomPriceColumn;

    private RoomData roomData;
    private HotelData hotelData; // NUEVO
    private ObjectMapper objectMapper;
    private ObservableList<Room> roomList = FXCollections.observableArrayList();
    private ObservableList<Hotel> hotelList = FXCollections.observableArrayList(); // NUEVO
    private Room selectedRoom;
    private Hotel selectedHotel;
    private boolean editMode = false;
    private final SocketCliente socketCliente;

    public RoomManagementController() {
        socketCliente = new SocketCliente(new SocketCliente.ClienteCallback() {
            @Override public void onMensajeRecibido(String mensaje) {
                Platform.runLater(() -> procesarRespuestaServidor(mensaje));
            }
            @Override public void onError(String error) {
                Platform.runLater(() -> FXUtility.alertError("Error de comunicación", error).show());
            }
            @Override public void onConexionEstablecida() {
                Platform.runLater(() -> loadRoomsFromServer());
            }
            @Override public void onDesconexion() {
                Platform.runLater(() -> FXUtility.alertError("Desconexión", "Se perdió la conexión con el servidor").show());
            }
        });
    }

    @FXML
    private void initialize() {
        try {
            roomData = DataFactory.getRoomData();
            hotelData = DataFactory.getHotelData(); // NUEVO
            objectMapper = new ObjectMapper();

            setupControls();
            setFieldsEnabled(false);
            roomTableView.setItems(roomList);

            roomNumberColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoomNumber()));
            roomTypeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoomType().toString()));
            roomCapacityColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCapacity()).asObject());
            roomPriceColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getPrice()).asObject());
            roomStatusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoomCondition().toString()));

            hotelComboBox.setItems(hotelList); 
            hotelComboBox.setConverter(new StringConverter<Hotel>() {
                @Override public String toString(Hotel hotel) {
                    return hotel != null ? hotel.getName() : "";
                }

                @Override public Hotel fromString(String string) {
                    return null;
                }
            });
            hotelComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                selectedHotel = newVal;
                loadRoomsFromFile();
            });

            loadHotels(); 

            roomTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                selectedRoom = newVal;
                handleRoomSelection();
            });

            searchTextField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        } catch (Exception e) {
            FXUtility.alertError("Error de Inicialización", "No se pudieron cargar los datos: " + e.getMessage()).show();
        }
    }
    

    private void loadHotels() {
        try {
            String json = hotelData.readAll();
            JsonResponse response = objectMapper.readValue(json, JsonResponse.class);
            if (response.isSuccess()) {
                List<Hotel> hotels = objectMapper.convertValue(response.getData(), new TypeReference<List<Hotel>>() {});
                hotelList.setAll(hotels);
    
                if (!hotelList.isEmpty()) {
                    hotelComboBox.getSelectionModel().selectFirst(); // ← Selecciona automáticamente el primer hotel
                }
            }
        } catch (Exception e) {
            FXUtility.alertError("Error", "No se pudieron cargar los hoteles: " + e.getMessage()).show();
        }
    }
    

    private void setupControls() {
        typeComboBox.setItems(FXCollections.observableArrayList(RoomType.values()));
        statusComboBox.setItems(FXCollections.observableArrayList(RoomCondition.values()));
        capacitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
    }

    public void setSelectedHotel(Hotel hotel) {
        this.selectedHotel = hotel;
        loadRoomsFromFile();
    }

    private void loadRoomsFromFile() {

        if (selectedHotel == null) {
            FXUtility.alertError("Error", "No hay hotel seleccionado.").show();
            return;
        }
        
        try {

            
            String json = roomData.readAll();
            JsonResponse res = objectMapper.readValue(json, JsonResponse.class);
    
            if (res.isSuccess()) {
                List<?> rawRooms = (List<?>) res.getData();
                List<Room> allRooms = objectMapper.convertValue(
                    rawRooms,
                    new TypeReference<List<Room>>() {}
                );
                List<Room> filtered = allRooms.stream()
                    .filter(r -> r.getHotel().getHotelId() == selectedHotel.getHotelId())
                    .collect(Collectors.toList());
    
                roomList.setAll(filtered);
            }
    
        } catch (Exception e) {
            FXUtility.alertError("Error", "No se pudieron cargar las habitaciones.").show();
        }
    }
    

    @FXML
    private void handleAddRoom() {
        clearFields();
        setFieldsEnabled(true);
        editMode = false;
        statusLabel.setText("Agregando nueva habitación...");
    }

    @FXML
    private void handleEditRoom() {
        if (selectedRoom != null) {
            setFieldsEnabled(true);
            editMode = true;
            statusLabel.setText("Editando habitación...");
        }
    }

    @FXML
    private void handleDeleteRoom() {
        if (selectedRoom == null) {
            System.out.println("[DeleteRoom] No hay habitación seleccionada.");
            return;
        }
    
        Alert alert = new Alert(
            Alert.AlertType.CONFIRMATION,
            "¿Eliminar habitación " + selectedRoom.getRoomNumber() + "?",
            ButtonType.YES,
            ButtonType.NO
        );
        alert.setTitle("Confirmar Eliminación");
    
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    String json = roomData.delete(selectedRoom.getRoomNumber(), selectedRoom.getHotel().getHotelId());
                    System.out.println("[DeleteRoom] JSON devuelto por delete: " + json);
    
                    // Usa TypeReference para evitar problemas con genéricos
                    JsonResponse<Object> res = objectMapper.readValue(
                        json, new TypeReference<JsonResponse<Object>>() {}
                    );
    
                    System.out.println("[DeleteRoom] Success: " + res.isSuccess());
                    System.out.println("[DeleteRoom] Message: " + res.getMessage());
    
                    if (res.isSuccess()) {
                        boolean removed = roomList.removeIf(
                            r -> r.getRoomNumber().trim().equalsIgnoreCase(selectedRoom.getRoomNumber().trim())
                        );
    
                        System.out.println("[DeleteRoom] Habitación en lista fue eliminada: " + removed);
    
                        roomTableView.refresh();
                        clearFields();
                        selectedRoom = null;
                        statusLabel.setText("Habitación eliminada.");
                    } else {
                        FXUtility.alertError("Error", res.getMessage()).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    FXUtility.alertError("Error", "Error al eliminar: " + e.getMessage()).show();
                }
            }
        });
    }
    
    @FXML
    private void handleSave() {
        if (!validateFields()) return;
    
        if (selectedHotel == null) {
            FXUtility.alertError("Error", "No hay hotel seleccionado.").show();
            return;
        }
    
        try {
            Room newRoom = new Room(
                    numberTextField.getText(),
                    selectedHotel,
                    typeComboBox.getValue(),
                    statusComboBox.getValue(),
                    Double.parseDouble(priceTextField.getText()),
                    capacitySpinner.getValue(),
                    featuresTextArea.getText(),
                    descriptionTextArea.getText()
            );
    
            String json = editMode ? roomData.update(newRoom) : roomData.create(newRoom);
            JsonResponse res = objectMapper.readValue(json, JsonResponse.class);
    
            if (res.isSuccess()) {
                System.out.println("Habitación guardada: " + newRoom.getRoomNumber() +
                                   " HotelId: " + newRoom.getHotel().getHotelId());
    
                loadRoomsFromFile();  // ← Asegura que se cargue desde archivo actualizado
                roomTableView.refresh();
    
                setFieldsEnabled(false);
                clearFields();
                selectedRoom = null;
                editMode = false;
                statusLabel.setText("Habitación guardada con éxito.");
            } else {
                FXUtility.alertError("Error", res.getMessage()).show();
            }
    
        } catch (Exception e) {
            FXUtility.alertError("Error", "Error al guardar: " + e.getMessage()).show();
        }
    }
    

    @FXML
    private void handleCancel() {
        clearFields();
        setFieldsEnabled(false);
        selectedRoom = null;
        editMode = false;
        statusLabel.setText("Operación cancelada.");
    }

    @FXML
    private void handleRoomSelection() {
        if (selectedRoom != null) {
            numberTextField.setText(selectedRoom.getRoomNumber());
            typeComboBox.setValue(selectedRoom.getRoomType());
            statusComboBox.setValue(selectedRoom.getRoomCondition());
            descriptionTextArea.setText(selectedRoom.getDescription());
            featuresTextArea.setText(selectedRoom.getFeatures());
            priceTextField.setText(String.valueOf(selectedRoom.getPrice()));
            capacitySpinner.getValueFactory().setValue(selectedRoom.getCapacity());
            editButton.setDisable(false);
            deleteButton.setDisable(false);
        }
    }

    private void clearFields() {
        numberTextField.clear();
        typeComboBox.setValue(null);
        statusComboBox.setValue(null);
        descriptionTextArea.clear();
        featuresTextArea.clear();
        priceTextField.clear();
        capacitySpinner.getValueFactory().setValue(1);
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void setFieldsEnabled(boolean enabled) {
        numberTextField.setDisable(!enabled);
        typeComboBox.setDisable(!enabled);
        statusComboBox.setDisable(!enabled);
        descriptionTextArea.setDisable(!enabled);
        featuresTextArea.setDisable(!enabled);
        priceTextField.setDisable(!enabled);
        capacitySpinner.setDisable(!enabled);
        saveButton.setDisable(!enabled);
        cancelButton.setDisable(!enabled);
        addButton.setDisable(enabled);
    }

    private boolean validateFields() {
        StringBuilder sb = new StringBuilder();
        if (numberTextField.getText().isEmpty()) sb.append("Número obligatorio.\n");
        if (typeComboBox.getValue() == null) sb.append("Tipo obligatorio.\n");
        if (statusComboBox.getValue() == null) sb.append("Estado obligatorio.\n");

        if (sb.length() > 0) {
            FXUtility.alertError("Campos inválidos", sb.toString()).show();
            return false;
        }
        return true;
    }

    @FXML
    private void handleSearch() {
        String query = searchTextField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            roomTableView.setItems(roomList);
        } else {
            ObservableList<Room> filtered = roomList.filtered(room ->
                room.getRoomNumber().toLowerCase().contains(query) ||
                room.getRoomType().name().toLowerCase().contains(query) ||
                room.getRoomCondition().name().toLowerCase().contains(query) ||
                String.valueOf(room.getCapacity()).contains(query) ||
                String.valueOf(room.getPrice()).contains(query)
            );
            roomTableView.setItems(filtered);
        }
    }


    private void procesarRespuestaServidor(String respuesta) {
        try {
            JsonResponse response = objectMapper.readValue(respuesta, JsonResponse.class);
            if (response.isSuccess()) {
                List<Room> rooms = (List<Room>) response.getData();
                actualizarTablaHabitaciones(rooms);
            } else {
                FXUtility.alertError("Error", response.getMessage()).show();
            }
        } catch (Exception e) {
            FXUtility.alertError("Error", "Respuesta inválida del servidor: " + e.getMessage()).show();
        }
    }

    private void actualizarTablaHabitaciones(List<Room> rooms) {
        roomList.setAll(rooms);
        roomTableView.setItems(roomList);
        roomTableView.refresh();
        statusLabel.setText(rooms.isEmpty() ? "Sin habitaciones." : "");
    }

    private void loadRoomsFromServer() {
        if (!socketCliente.estaConectado()) {
            FXUtility.alertError("Error", "Sin conexión al servidor").show();
            return;
        }
        socketCliente.enviarMensaje("OBTENER_HABITACIONES|" + selectedHotel.getHotelId());
    }

    public void handleClose(ActionEvent e) {}

    @Override
    public void close() {
        try {
            if (roomData != null) roomData.close();
            if (socketCliente != null) socketCliente.desconectar();
        } catch (Exception e) {
            FXUtility.alertError("Error", "Error al cerrar: " + e.getMessage()).show();
        }
    }
}

