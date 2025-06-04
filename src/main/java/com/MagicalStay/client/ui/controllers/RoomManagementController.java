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
    @FXML
    private TableView imagesTableView;
    @FXML
    private TableColumn imagePathColumn;
    @FXML
    private TableColumn imageNameColumn;
    @FXML
    private Button closeButton;

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
            String json = hotelData.retrieveAll();
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
            String jsonResponse = roomData.retrieveAll();
            DataResponse response = parseDataResponse(jsonResponse);
    
            if (response.isSuccess()) {
                List<Room> allRooms = objectMapper.convertValue(
                    response.getData(),
                    new TypeReference<List<Room>>() {}
                );
                List<Room> filteredRooms = allRooms.stream()
                    .filter(r -> r.getHotel() != null && r.getHotel().getHotelId() == selectedHotel.getHotelId())
                    .collect(Collectors.toList());
                roomList.setAll(filteredRooms);
            } else {
                roomList.clear();
                statusLabel.setText("No se encontraron habitaciones: " + response.getMessage());
            }
        } catch (Exception e) {
            roomList.clear();
            statusLabel.setText("Error al cargar habitaciones: " + e.getMessage());
            e.printStackTrace();
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
            FXUtility.alertError("Error", "No hay habitación seleccionada.").show();
            return;
        }
    
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText(null);
        alert.setContentText("¿Está seguro que desea eliminar la habitación \"" +
                             selectedRoom.getRoomNumber() + "\"?");
    
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String jsonResponse = roomData.delete(selectedRoom.getRoomNumber());
                DataResponse response = parseDataResponse(jsonResponse);
    
                if (response.isSuccess()) {
                    loadRoomsFromFile();
                    roomTableView.setItems(roomList);
                    clearFields();
    
                    selectedRoom = null;
                    statusLabel.setText("Habitación eliminada con éxito.");
    
                    // Deshabilitar botones si aplica
                    editButton.setDisable(true);
                    deleteButton.setDisable(true);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "No se pudo eliminar la habitación: " + response.getMessage());
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Error al eliminar la habitación: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }    
    
    @FXML
private void handleSave() {
    if (!validateFields()) return;

    if (selectedHotel == null) {
        FXUtility.alertError("Error", "No hay hotel seleccionado.").show();
        return;
    }

    try {
        Room room = new Room(
            numberTextField.getText(),
            selectedHotel,
            typeComboBox.getValue(),
            statusComboBox.getValue(),
            Double.parseDouble(priceTextField.getText()),
            capacitySpinner.getValue(),
            featuresTextArea.getText(),
            descriptionTextArea.getText()
        );

        String jsonResponse = editMode ? roomData.update(room) : roomData.create(room);
        DataResponse response = parseDataResponse(jsonResponse);

        if (response.isSuccess()) {
            loadRoomsFromFile();
            roomTableView.setItems(roomList);

            setFieldsEnabled(false);
            clearFields();
            saveButton.setDisable(true);
            cancelButton.setDisable(true);

            // Seleccionar la habitación recién guardada
            for (Room r : roomList) {
                if (r.getRoomNumber().equals(room.getRoomNumber())
                    && r.getHotel().getHotelId() == room.getHotel().getHotelId()) {
                    roomTableView.getSelectionModel().select(r);
                    break;
                }
            }

            selectedRoom = null;
            editMode = false;
            statusLabel.setText("Habitación guardada con éxito.");

            System.out.println("[handleSave] Habitación guardada: " + room.getRoomNumber() +
                               " | Hotel ID: " + room.getHotel().getHotelId() +
                               " | Tipo: " + room.getRoomType() +
                               " | Estado: " + room.getRoomCondition());

            System.out.println("[handleSave] Lista actual de habitaciones:");
            for (Room r : roomList) {
                System.out.println("- " + r.getRoomNumber() + " | hotelId: " +
                    (r.getHotel() != null ? r.getHotel().getHotelId() : "null"));
            }


        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                "No se pudo guardar la habitación: " + response.getMessage());
        }

    } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error",
            "Error al guardar la habitación: " + e.getMessage());
        e.printStackTrace();
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

    @FXML
    public void handleClose(ActionEvent e) throws IOException {
        DataFactory.closeAll();

        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void close() {
        try {
            if (roomData != null) roomData.close();
            if (socketCliente != null) socketCliente.desconectar();
        } catch (Exception e) {
            FXUtility.alertError("Error", "Error al cerrar: " + e.getMessage()).show();
        }
    }

    private DataResponse parseDataResponse(String jsonResponse) throws Exception {
        return objectMapper.readValue(jsonResponse, DataResponse.class);
    }

    private static class DataResponse {
        private boolean success;
        private String message;
        private Object data;

        // Getters y setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

