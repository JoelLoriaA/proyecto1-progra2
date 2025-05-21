package com.MagicalStay.ui.controllers;

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
import model.Room;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoomManagementController {

    private final ObservableList<Room> roomList = FXCollections.observableArrayList();
    private final ObservableList<String> hotelList = FXCollections.observableArrayList("Hotel A", "Hotel B");
    private final ObservableList<String> roomTypes = FXCollections.observableArrayList("Simple", "Doble", "Suite");
    private final ObservableList<String> roomStates = FXCollections.observableArrayList("Disponible", "Ocupado", "Mantenimiento");

    private Room selectedRoom = null;
    private final ObservableList<String> roomImages = FXCollections.observableArrayList();
    @FXML
    private CheckBox balconyCheckBox;
    @FXML
    private ComboBox roomStatusComboBox;
    @FXML
    private Button deleteButton;
    @FXML
    private CheckBox tvCheckBox;
    @FXML
    private TableColumn roomTypeColumn;
    @FXML
    private CheckBox minibarCheckBox;
    @FXML
    private TableColumn roomNumberColumn;
    @FXML
    private TableView roomListTableView;
    @FXML
    private FlowPane imagesFlowPane;
    @FXML
    private Button cancelButton;
    @FXML
    private Button closeButton;
    @FXML
    private ComboBox roomTypeComboBox;
    @FXML
    private TextField priceTextField;
    @FXML
    private CheckBox acCheckBox;
    @FXML
    private Button saveButton;
    @FXML
    private TextField roomNumberTextField;
    @FXML
    private ComboBox hotelComboBox;
    @FXML
    private ComboBox typeComboBox;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Button addButton;
    @FXML
    private Button removeImageButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Button addImageButton;
    @FXML
    private ComboBox adultsComboBox;
    @FXML
    private CheckBox wifiCheckBox;
    @FXML
    private ComboBox statusComboBox;
    @FXML
    private TableColumn roomStatusColumn;
    @FXML
    private Button editButton;
    @FXML
    private CheckBox safeCheckBox;
    @FXML
    private ComboBox roomHotelComboBox;
    @FXML
    private ComboBox childrenComboBox;

    @FXML
    public void initialize() {
        // Columnas
        hotelColumn.setCellValueFactory(data -> data.getValue().hotelNameProperty());
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());
        stateColumn.setCellValueFactory(data -> data.getValue().stateProperty());

        // Tabla
        roomTable.setItems(roomList);
        roomTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> handleRoomSelection(newVal));

        // ComboBoxes
        hotelFilter.setItems(FXCollections.observableArrayList(hotelList));
        hotelFilter.getItems().add(0, "Todos");

        stateFilter.setItems(FXCollections.observableArrayList(roomStates));
        stateFilter.getItems().add(0, "Todos");

        typeFilter.setItems(FXCollections.observableArrayList(roomTypes));
        typeFilter.getItems().add(0, "Todos");

        hotelCombo.setItems(hotelList);
        typeCombo.setItems(roomTypes);
        stateCombo.setItems(roomStates);

        loadSampleData();
        setInputFieldsDisabled(true);
    }

    private void loadSampleData() {
        roomList.addAll(
                new Room("Hotel A", 100.0, 2, "TV, WiFi", "Habitación cómoda", "Simple", "Disponible"),
                new Room("Hotel B", 150.0, 4, "A/C, Minibar", "Ideal para familias", "Doble", "Ocupado")
        );
    }

    @FXML
    private void handleRoomSelection(Room room) {
        selectedRoom = room;
        if (room != null) {
            populateRoomDetails(room);
            setInputFieldsDisabled(true);
        }
    }

    private void populateRoomDetails(Room room) {
        priceField.setText(String.valueOf(room.getPrice()));
        capacityField.setText(String.valueOf(room.getCapacity()));
        characteristicsArea.setText(room.getCharacteristics());
        descriptionArea.setText(room.getDescription());
        hotelCombo.setValue(room.getHotelName());
        typeCombo.setValue(room.getType());
        stateCombo.setValue(room.getState());

        imagePane.getChildren().clear();
        if (room.getImages() != null) {
            for (String imagePath : room.getImages()) {
                addImageToPane(imagePath);
            }
        }
    }

    private void setInputFieldsDisabled(boolean disabled) {
        priceField.setDisable(disabled);
        capacityField.setDisable(disabled);
        characteristicsArea.setDisable(disabled);
        descriptionArea.setDisable(disabled);
        hotelCombo.setDisable(disabled);
        typeCombo.setDisable(disabled);
        stateCombo.setDisable(disabled);
    }

    private void clearRoomDetails() {
        priceField.clear();
        capacityField.clear();
        characteristicsArea.clear();
        descriptionArea.clear();
        hotelCombo.setValue(null);
        typeCombo.setValue(null);
        stateCombo.setValue(null);
        imagePane.getChildren().clear();
        roomImages.clear();
    }

    @FXML
    private void handleAddRoom() {
        selectedRoom = null;
        clearRoomDetails();
        setInputFieldsDisabled(false);
    }

    @FXML
    private void handleEditRoom() {
        if (selectedRoom != null) {
            setInputFieldsDisabled(false);
        }
    }

    @FXML
    private void handleDeleteRoom() {
        if (selectedRoom != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Deseas eliminar esta habitación?", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Confirmación");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                roomList.remove(selectedRoom);
                selectedRoom = null;
                clearRoomDetails();
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateRoomDetails()) return;

        String hotel = hotelCombo.getValue();
        String type = typeCombo.getValue();
        String state = stateCombo.getValue();
        double price = Double.parseDouble(priceField.getText());
        int capacity = Integer.parseInt(capacityField.getText());
        String characteristics = characteristicsArea.getText();
        String description = descriptionArea.getText();

        if (selectedRoom == null) {
            Room newRoom = new Room(hotel, price, capacity, characteristics, description, type, state);
            newRoom.setImages(List.copyOf(roomImages));
            roomList.add(newRoom);
        } else {
            selectedRoom.setHotelName(hotel);
            selectedRoom.setType(type);
            selectedRoom.setState(state);
            selectedRoom.setPrice(price);
            selectedRoom.setCapacity(capacity);
            selectedRoom.setCharacteristics(characteristics);
            selectedRoom.setDescription(description);
            selectedRoom.setImages(List.copyOf(roomImages));
            roomTable.refresh();
        }

        setInputFieldsDisabled(true);
        clearRoomDetails();
    }

    @FXML
    private void handleCancel() {
        clearRoomDetails();
        setInputFieldsDisabled(true);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) roomTable.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleHotelFilter() { applyFilters(); }

    @FXML
    private void handleStateFilter() { applyFilters(); }

    @FXML
    private void handleTypeFilter() { applyFilters(); }

    private void applyFilters() {
        String hotel = hotelFilter.getValue();
        String state = stateFilter.getValue();
        String type = typeFilter.getValue();

        List<Room> filtered = roomList.stream()
                .filter(r -> (hotel == null || hotel.equals("Todos") || r.getHotelName().equals(hotel)))
                .filter(r -> (state == null || state.equals("Todos") || r.getState().equals(state)))
                .filter(r -> (type == null || type.equals("Todos") || r.getType().equals(type)))
                .collect(Collectors.toList());

        roomTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(roomTable.getScene().getWindow());

        if (selectedFile != null) {
            String path = selectedFile.toURI().toString();
            roomImages.add(path);
            addImageToPane(path);
        }
    }

    private void addImageToPane(String imagePath) {
        ImageView imageView = new ImageView(new Image(imagePath, 100, 100, true, true));
        imageView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar esta imagen?", ButtonType.YES, ButtonType.NO);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.YES) {
                    roomImages.remove(imagePath);
                    imagePane.getChildren().remove(imageView);
                }
            }
        });
        imagePane.getChildren().add(imageView);
    }

    private boolean validateRoomDetails() {
        try {
            Double.parseDouble(priceField.getText());
            Integer.parseInt(capacityField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error de validación", "Precio y capacidad deben ser numéricos.");
            return false;
        }

        if (hotelCombo.getValue() == null || typeCombo.getValue() == null || stateCombo.getValue() == null) {
            showAlert("Error de validación", "Debes seleccionar hotel, tipo y estado.");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleRemoveImage(ActionEvent actionEvent) {
    }

    @FXML
    public void handleStatusFilter(ActionEvent actionEvent) {
    }
}