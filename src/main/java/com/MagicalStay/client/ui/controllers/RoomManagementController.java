package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.sockets.SocketCliente;
import com.MagicalStay.client.data.DataFactory;
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

import java.io.File;
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
    @FXML
    private TextField searchTextField;
    @FXML
    private TextField numberTextField;
    @FXML
    private ComboBox<RoomType> typeComboBox;
    @FXML
    private ComboBox<RoomCondition> statusComboBox;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private TextArea featuresTextArea;
    @FXML
    private TextField priceTextField;
    @FXML
    private Spinner<Integer> capacitySpinner;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label statusLabel;

    @FXML
    private TableColumn<Room, String> roomNumberColumn;
    @FXML
    private TableColumn<Room, String> roomTypeColumn;
    @FXML
    private TableColumn<Room, Integer> roomCapacityColumn;
    @FXML
    private TableColumn<Room, Double> roomPriceColumn;
    @FXML
    private TableColumn<Room, String> roomStatusColumn;

    private RoomData roomData;
    private ObjectMapper objectMapper;
    private ObservableList<Room> roomList;
    private Room selectedRoom;
    private Hotel selectedHotel;
    private boolean editMode = false;

    private final SocketCliente socketCliente;
    @FXML
    private Button searchButton;
    @FXML
    private TableView<Room> roomTableView;

    public RoomManagementController() {
        socketCliente = new SocketCliente(new SocketCliente.ClienteCallback() {
            @Override
            public void onMensajeRecibido(String mensaje) {
                Platform.runLater(() -> procesarRespuestaServidor(mensaje));
            }

            @Override
            public void onError(String error) {
                Platform.runLater(() ->
                        FXUtility.alertError("Error de comunicación", error).show());
            }

            @Override
            public void onConexionEstablecida() {
                Platform.runLater(() -> loadRoomsFromServer());
            }

            @Override
            public void onDesconexion() {
                Platform.runLater(() ->
                        FXUtility.alertError("Desconexión",
                                "Se perdió la conexión con el servidor").show());
            }
        });
    }

    private void loadRoomsFromServer() {
        if (!socketCliente.estaConectado()) {
            FXUtility.alertError("Error",
                    "No hay conexión con el servidor").show();
            return;
        }

        try {
            String comando = "OBTENER_HABITACIONES|" + selectedHotel.getHotelId();
            socketCliente.enviarMensaje(comando);
        } catch (Exception e) {
            FXUtility.alertError("Error",
                    "Error al solicitar habitaciones: " + e.getMessage()).show();
        }
    }

    private void procesarRespuestaServidor(String respuesta) {
        try {
            JsonResponse response = objectMapper.readValue(respuesta, JsonResponse.class);
            if (response.isSuccess()) {
                actualizarTablaHabitaciones((List<Room>) response.getData());
            } else {
                FXUtility.alertError("Error", response.getMessage()).show();
            }
        } catch (Exception e) {
            FXUtility.alertError("Error",
                    "Error procesando respuesta: " + e.getMessage()).show();
        }
    }

    @FXML
    private void initialize() {
        try {
            roomData = DataFactory.getRoomData();
            objectMapper = new ObjectMapper();

            setupControls();
            setFieldsEnabled(false);

            // Configurar las columnas del TableView
            roomNumberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomNumber()));
            roomTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomType().toString()));
            roomCapacityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCapacity()).asObject());
            roomPriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
            roomStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomCondition().toString()));

            searchTextField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());

            // Deshabilitar botones inicialmente
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            saveButton.setDisable(true);
            cancelButton.setDisable(true);

            if (selectedHotel != null) {
                loadRoomsFromFile();
            }

        } catch (Exception e) {
            FXUtility.alertError("Error de Inicialización", "No se pudieron cargar los datos: " + e.getMessage());
        }
    }


    @FXML
    private void handleSearch() {
        String searchText = searchTextField.getText().toLowerCase().trim();

        try {
            String jsonResponse = roomData.readAll();
            JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

            if (response.isSuccess()) {
                List<Room> rooms = objectMapper.convertValue(response.getData(), new TypeReference<List<Room>>() {});

                // Filtrar por hotel seleccionado
                rooms = rooms.stream()
                        .filter(room -> room.getHotel().getHotelId() == selectedHotel.getHotelId())
                        .collect(Collectors.toList());

                // Aplicar filtro de búsqueda si hay texto
                if (!searchText.isEmpty()) {
                    rooms = rooms.stream()
                            .filter(room ->
                                    room.getRoomNumber().toLowerCase().contains(searchText) ||
                                            room.getRoomType().toString().toLowerCase().contains(searchText) ||
                                            room.getRoomCondition().toString().toLowerCase().contains(searchText))
                            .collect(Collectors.toList());
                }

                roomList = FXCollections.observableArrayList(rooms);
                roomTableView.setItems(roomList);

                if (rooms.isEmpty()) {
                    statusLabel.setText("No se encontraron habitaciones que coincidan con la búsqueda");
                } else {
                    statusLabel.setText("");
                }
            } else {
                statusLabel.setText("Error en la búsqueda: " + response.getMessage());
            }
        } catch (Exception e) {
            statusLabel.setText("Error al realizar la búsqueda: " + e.getMessage());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error en búsqueda de habitaciones", e);
        }
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

        // Solo habilitar estos botones si hay una habitación seleccionada
        if (selectedRoom != null) {
            editButton.setDisable(!enabled);
            deleteButton.setDisable(!enabled);
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
            FXUtility.alertInformation("Advertencia", "No hay hotel seleccionado");
            return;
        }

        try {
            String jsonResponse = roomData.readAll();
            JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

            if (response.isSuccess()) {
                List<Room> rooms = objectMapper.convertValue(response.getData(), new TypeReference<List<Room>>() {});

                // Filtrar habitaciones por hotel si hay uno seleccionado
                rooms = rooms.stream()
                        .filter(room -> room.getHotel().getHotelId() == selectedHotel.getHotelId())
                        .collect(Collectors.toList());

                roomList = FXCollections.observableArrayList(rooms);
                roomTableView.setItems(roomList);
            } else {
                roomList = FXCollections.observableArrayList();
                statusLabel.setText("No se encontraron habitaciones: " + response.getMessage());
            }
        } catch (Exception e) {
            roomList = FXCollections.observableArrayList();
            statusLabel.setText("Error al cargar habitaciones: " + e.getMessage());
        }
    }

    @FXML
    private void handleRoomSelection() {
        selectedRoom = roomTableView.getSelectionModel().getSelectedItem();
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


    @FXML
    private void handleAddRoom() {
        clearFields();
        setFieldsEnabled(true);
        editMode = false;
        saveButton.setDisable(false);
        cancelButton.setDisable(false);
        statusLabel.setText("Agregando nueva habitación...");
    }

    private void clearFields() {
        numberTextField.clear();
        typeComboBox.setValue(null);
        statusComboBox.setValue(null);
        descriptionTextArea.clear();
        featuresTextArea.clear();
        priceTextField.clear();
        capacitySpinner.getValueFactory().setValue(1);

        selectedRoom = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @FXML
    private void handleEditRoom() {
        if (selectedRoom != null) {
            setFieldsEnabled(true);
            editMode = true;
            saveButton.setDisable(false);
            cancelButton.setDisable(false);
            statusLabel.setText("Editando habitación: " + selectedRoom.getRoomNumber());
        }
    }

    @FXML
    private void handleDeleteRoom() {
        if (selectedRoom != null) {
            Alert alert = FXUtility.alertInformation(
                    "Confirmar Eliminación",
                    "¿Está seguro que desea eliminar la habitación \"" + selectedRoom.getRoomNumber() + "\"?"
            );

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String jsonResponse = roomData.delete(selectedRoom.getRoomNumber());
                    JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

                    if (response.isSuccess()) {
                        loadRoomsFromFile();  // Recargar la lista después de eliminar
                        clearFields();
                        statusLabel.setText("Habitación eliminada con éxito");
                    } else {
                        FXUtility.alertError("Error", "No se pudo eliminar la habitación: " + response.getMessage()).show();
                    }
                } catch (Exception e) {
                    FXUtility.alertError("Error", "Error al eliminar la habitación: " + e.getMessage()).show();
                }
            }
        }
    }


    @FXML
    private Hotel selectedHotel2;

    public void handleSave() {
        
        if (selectedHotel == null) {
            Random random = new Random();

            // 1. Create Dummy Guests
            List<Guest> dummyGuests = new ArrayList<>();
            for (int i = 0; i < 2; i++) { // Let's create 2 dummy guests
                dummyGuests.add(new Guest(
                        "Guest" + (i + 1),
                        "Lastname" + (i + 1),
                        100000000 + random.nextInt(900000000), // Random DNI
                        80000000 + random.nextInt(20000000), // Random Phone Number
                        "guest" + (i + 1) + "@example.com",
                        "Dummy Address " + (i + 1),
                        (i == 0 ? "Costa Rican" : "Nicaraguan") // Example nationalities
                ));
            }

            // 2. Create Dummy Rooms (these rooms will refer to the hotel we are about to create)
            // Note: For creating rooms that refer to the *newly created* hotel,
            // we'll first create the hotel, then add rooms to its list.
            // For now, let's just prepare the lists.
            List<Room> dummyRooms = new ArrayList<>();


            // 3. Create the Dummy Hotel (selectedHotel)
            selectedHotel2 = new Hotel(
                    23, // Dummy hotelId
                    "Dummy Test Hotel", // Dummy name
                    "123 Testing Ave, Test City", // Dummy address
                    dummyRooms, // Pass the empty list initially, then add rooms to it
                    dummyGuests // Pass the populated list of dummy guests
            );

            // 4. Now, populate dummyRooms using the newly created selectedHotel
            dummyRooms.add(new Room(
                    "101",
                    RoomType.ESTANDAR, // Assuming RoomType is an enum, e.g., RoomType.STANDARD
                    RoomCondition.DISPONIBLE, // Assuming RoomCondition is an enum, e.g., RoomCondition.CLEAN
                    selectedHotel2 // Associate with the dummy hotel
            ));
            dummyRooms.add(new Room(
                    "205",
                    RoomType.DELUXE,
                    RoomCondition.DISPONIBLE,
                    selectedHotel2
            ));
            dummyRooms.add(new Room(
                    "300",
                    RoomType.SUITE,
                    RoomCondition.DISPONIBLE,
                    selectedHotel2
            ));


            System.out.println("DEBUG: selectedHotel was null, assigned a dummy hotel with dummy data for testing.");
        }
        // --- END TEMPORARY BLOCK ---

        // Validar campos antes de guardar
        if (!validateFields()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Por favor complete todos los campos");
            alert.show();
            return;
        }

        try {
            Room room = new Room(
                    numberTextField.getText(),
                    typeComboBox.getValue(),
                    statusComboBox.getValue(),
                    selectedHotel2 // This will now be your dummy hotel if no real one is selected
            );

            String jsonResponse = editMode ? roomData.update(room) : roomData.create(room);
            JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

            if (response != null && response.isSuccess()) {
                if (!editMode) {
                    roomList.add(room);
                } else {
                    int index = roomList.indexOf(selectedRoom);
                    if (index != -1) {
                        roomList.set(index, room);
                    }
                }

                Platform.runLater(() -> roomTableView.setItems(roomList));

                // Restablecer el estado de los campos
                setFieldsEnabled(false);
                saveButton.setDisable(true);
                cancelButton.setDisable(true);
                statusLabel.setText("Habitación guardada con éxito");

                // Limpiar la selección de la habitación
                selectedRoom = null;

            } else {
                String errorMessage = response != null ? response.getMessage() : "Error desconocido";
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("No se pudo guardar la habitación: " + errorMessage);
                alert.show();
            }
        } catch (JsonProcessingException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error al procesar la respuesta JSON: " + e.getMessage());
            alert.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error al guardar la habitación: " + e.getMessage());
            alert.show();
        }
    }

    // ... (rest of your controller methods like validateFields, setFieldsEnabled, etc.)



    private boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();

        if (numberTextField.getText().trim().isEmpty()) {
            errorMessage.append("El número de habitación no puede estar vacío.\n");
        }
        if (typeComboBox.getValue() == null) {
            errorMessage.append("Debe seleccionar un tipo de habitación.\n");
        }
        if (statusComboBox.getValue() == null) {
            errorMessage.append("Debe seleccionar un estado de habitación.\n");
        }

        if (errorMessage.length() > 0) {
            FXUtility.alertError("Error de Validación", errorMessage.toString()).show();
            return false;
        }
        return true;
    }

    @FXML
    public void handleCancel() {
        if (selectedRoom != null) {
            handleRoomSelection();
        } else {
            clearFields();
        }
        setFieldsEnabled(false);
        editMode = false;
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        statusLabel.setText("");
    }

    private void actualizarTablaHabitaciones(List<Room> rooms) {
        // Crear la lista observable con las habitaciones
        roomList = FXCollections.observableArrayList(rooms);

        // Configurar la tabla para mostrar la lista de habitaciones
        roomTableView.setItems(roomList);

        // Si no hay habitaciones, mostramos un mensaje en la etiqueta de estado
        if (rooms.isEmpty()) {
            statusLabel.setText("No se encontraron habitaciones disponibles.");
        } else {
            statusLabel.setText("");  // Limpiar mensaje de estado
        }
    }

    public void handleClose(ActionEvent actionEvent) {
    }


    private static class JsonResponse {
        private boolean success;
        private String message;
        private Object data;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }

    @Override
    public void close() {
        try {
            if (roomData != null) {
                roomData.close();
            }
            if (socketCliente != null) {
                socketCliente.desconectar();
            }
        } catch (Exception e) {
            FXUtility.alertError("Error", "Error al cerrar recursos: " + e.getMessage()).show();
        }
    }
}
