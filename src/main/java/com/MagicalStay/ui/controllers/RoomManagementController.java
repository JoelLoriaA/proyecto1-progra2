package com.MagicalStay.ui.controllers;

import com.MagicalStay.data.DataFactory;
import com.MagicalStay.data.RoomData;
import com.MagicalStay.domain.Room;
import com.MagicalStay.domain.RoomType;
import com.MagicalStay.domain.RoomCondition;
import com.MagicalStay.domain.Hotel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.Closeable;

public class RoomManagementController implements Closeable {
    @FXML
    private TextField searchTextField;
    @FXML
    private TableView<Room> roomTableView;
    @FXML
    private TableColumn<Room, String> roomNumberColumn;
    @FXML
    private TableColumn<Room, String> roomTypeColumn;
    @FXML
    private TableColumn<Room, String> roomStatusColumn;
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
    private FlowPane imagePane;
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

    private RoomData roomData;
    private ObjectMapper objectMapper;
    private ObservableList<Room> roomList;
    private Room selectedRoom;
    private Hotel selectedHotel;
    private boolean editMode = false;
    private final ObservableList<String> roomImages = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        try {
            roomData = DataFactory.getRoomData();
            objectMapper = new ObjectMapper();

            setupControls();
            setupTableColumns();
            setFieldsEnabled(false);

            // Configurar listener para búsqueda
            searchTextField.textProperty().addListener((observable, oldValue, newValue) ->
                    handleSearch());

            // Configurar listener para selección de tabla
            roomTableView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            handleRoomSelection();
                        }
                    });

            // Deshabilitar botones inicialmente
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            saveButton.setDisable(true);
            cancelButton.setDisable(true);

        } catch (Exception e) {
            com.MagicalStay.util.FXUtility.alertError("Error de Inicialización",
                    "No se pudieron cargar los datos: " + e.getMessage());
        }

    }

    @FXML
    private void handleSearch() {
        String searchText = searchTextField.getText().toLowerCase().trim();

        try {
            String jsonResponse = roomData.readAll();
            JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

            if (response.isSuccess()) {
                List<Room> rooms = objectMapper.convertValue(response.getData(),
                        new TypeReference<List<Room>>() {
                        });

                // Filtrar por hotel seleccionado
                rooms = rooms.stream()
                        .filter(room -> room.getHotel().getHotelId() == selectedHotel.getHotelId())
                        .collect(java.util.stream.Collectors.toList());

                // Aplicar filtro de búsqueda si hay texto
                if (!searchText.isEmpty()) {
                    rooms = rooms.stream()
                            .filter(room ->
                                    room.getRoomNumber().toLowerCase().contains(searchText) ||
                                            room.getRoomType().toString().toLowerCase().contains(searchText) ||
                                            room.getRoomCondition().toString().toLowerCase().contains(searchText))
                            .collect(java.util.stream.Collectors.toList());
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
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error en búsqueda de habitaciones", e);
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

        capacitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
    }

    private void setupTableColumns() {
        roomNumberColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getRoomNumber()));

        roomTypeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getRoomType().toString()));

        roomStatusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getRoomCondition().toString()));
    }

    public void setSelectedHotel(Hotel hotel) {
        this.selectedHotel = hotel;
        loadRoomsFromFile();
    }

    private void loadRoomsFromFile() {
        if (selectedHotel == null) {
            com.MagicalStay.util.FXUtility.alertInformation("Advertencia",
                    "No hay hotel seleccionado");
            return;
        }

        try {
            String jsonResponse = roomData.readAll();
            JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

            if (response.isSuccess()) {
                List<Room> rooms = objectMapper.convertValue(response.getData(),
                        new TypeReference<List<Room>>() {
                        });

                // Filtrar habitaciones por hotel si hay uno seleccionado
                if (selectedHotel != null) {
                    rooms = rooms.stream()
                            .filter(room -> room.getHotel().getHotelId() == selectedHotel.getHotelId())
                            .collect(java.util.stream.Collectors.toList());
                }

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

            // Limpiar los campos no soportados
            descriptionTextArea.clear();
            featuresTextArea.clear();
            priceTextField.clear();
            capacitySpinner.getValueFactory().setValue(1);

            // Limpiar imágenes
            imagePane.getChildren().clear();
            roomImages.clear();

            editButton.setDisable(false);
            deleteButton.setDisable(false);
        }
    }

    private void loadRoomImages(Room room) {
        imagePane.getChildren().clear();
        roomImages.clear();
        if (room.getImages() != null) {
            roomImages.addAll(room.getImages());
            for (String imagePath : room.getImages()) {
                addImageToPane(imagePath);
            }
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
        imagePane.getChildren().clear();
        roomImages.clear();

        // Resetear selección y botones
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
            Alert alert = com.MagicalStay.util.FXUtility.alertInformation(
                    "Confirmar Eliminación",
                    "¿Está seguro que desea eliminar la habitación \"" + selectedRoom.getRoomNumber() + "\"?"
            );

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String jsonResponse = roomData.delete(selectedRoom.getRoomNumber());
                    JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

                    if (response.isSuccess()) {
                        loadRoomsFromFile();
                        clearFields();
                        statusLabel.setText("Habitación eliminada con éxito");
                    } else {
                        com.MagicalStay.util.FXUtility.alertError(
                                "Error",
                                "No se pudo eliminar la habitación: " + response.getMessage()
                        ).show();
                    }
                } catch (Exception e) {
                    com.MagicalStay.util.FXUtility.alertError(
                            "Error",
                            "Error al eliminar la habitación: " + e.getMessage()
                    ).show();
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateFields() || selectedHotel == null) {
            com.MagicalStay.util.FXUtility.alertError(
                    "Error",
                    "Por favor seleccione un hotel y complete todos los campos"
            ).show();
            return;
        }

        try {
            Room room = new Room(
                    numberTextField.getText(),
                    typeComboBox.getValue(),
                    statusComboBox.getValue(),
                    selectedHotel
            );

            String jsonResponse = editMode ? roomData.update(room) : roomData.create(room);
            JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

            if (response.isSuccess()) {
                loadRoomsFromFile();
                setFieldsEnabled(false);
                saveButton.setDisable(true);
                cancelButton.setDisable(true);
                statusLabel.setText("Habitación guardada con éxito");
            } else {
                com.MagicalStay.util.FXUtility.alertError(
                        "Error",
                        "No se pudo guardar la habitación: " + response.getMessage()
                ).show();
            }
        } catch (Exception e) {
            com.MagicalStay.util.FXUtility.alertError(
                    "Error",
                    "Error al guardar la habitación: " + e.getMessage()
            ).show();
        }
    }

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
            com.MagicalStay.util.FXUtility.alertError(
                    "Error de Validación",
                    errorMessage.toString()
            ).show();
            return false;
        }
        return true;
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
            // Limpiar otros recursos
            imagePane.getChildren().clear();
            roomImages.clear();
        } catch (Exception e) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error al cerrar recursos", e);
        }
    }

    private void addImageToPane(String imagePath) {
        try {
            Image image = new Image(new File(imagePath).toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(100);
            imageView.setFitWidth(100);
            imageView.setPreserveRatio(true);

            // Agregar menú contextual para eliminar imagen
            imageView.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    roomImages.remove(imagePath);
                    imagePane.getChildren().remove(imageView);
                }
            });

            imagePane.getChildren().add(imageView);
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Error al cargar imagen: " + imagePath, e);
        }
    }

    @FXML
    private void handleCancel() {
        if (selectedRoom != null) {
            // Restaurar valores originales
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
}
