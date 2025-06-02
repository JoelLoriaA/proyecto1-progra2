package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.shared.data.HotelData;
import com.MagicalStay.shared.data.RoomData;
import com.MagicalStay.shared.domain.Hotel;
import com.MagicalStay.shared.domain.Room;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HotelManagementController {

    // FXML elements for hotel list
    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<Hotel> hotelListView;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    // FXML elements for hotel details
    @FXML
    private TextField codeTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField locationTextField;

    @FXML
    private TextArea addressTextArea;

    @FXML
    private TextField phoneTextField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private CheckBox wifiCheckBox;

    @FXML
    private CheckBox poolCheckBox;

    @FXML
    private CheckBox gymCheckBox;

    @FXML
    private CheckBox restaurantCheckBox;

    @FXML
    private CheckBox parkingCheckBox;

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button manageRoomsButton;

    // FXML elements for room table
    @FXML
    private TableView<Room> roomsTableView;

    @FXML
    private TableColumn<Room, String> roomNumberColumn;

    @FXML
    private TableColumn<Room, String> roomTypeColumn;

    @FXML
    private TableColumn<Room, String> roomStatusColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Button closeButton;

    @FXML
    private Button searchButton;

    // Data
    private ObservableList<Hotel> hotelList;
    private ObservableList<Room> roomList;
    private Hotel selectedHotel;
    private boolean editMode = false;

    // Data access objects
    private HotelData hotelData;
    private RoomData roomData;
    private ObjectMapper objectMapper;

    @FXML
    private void initialize() {
        try {
            // Initialize data access objects
            hotelData = DataFactory.getHotelData();
            roomData = DataFactory.getRoomData();
            objectMapper = new ObjectMapper();

            // Initialize category combo box
            categoryComboBox.setItems(FXCollections.observableArrayList(
                    "1 Estrella", "2 Estrellas", "3 Estrellas", "4 Estrellas", "5 Estrellas"
            ));

            // Setup room table columns
            roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
            roomTypeColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getRoomType().toString()));
            roomStatusColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getRoomCondition().toString()));

            // Load data from files
            loadHotelsFromFile();

            // Set the hotel list view items
            hotelListView.setItems(hotelList);

            // Set cell factory to display hotel names in the list
            hotelListView.setCellFactory(lv -> new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel hotel, boolean empty) {
                    super.updateItem(hotel, empty);
                    if (empty || hotel == null) {
                        setText(null);
                    } else {
                        setText(hotel.getName() + " (" + hotel.getAddress() + ")");
                    }
                }
            });

            // Disable detail fields initially
            setFieldsEnabled(false);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Inicialización",
                    "No se pudieron cargar los datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadHotelsFromFile() {
        try {
            String jsonResponse = hotelData.readAll();
            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                List<Hotel> hotels = objectMapper.convertValue(response.getData(),
                        new TypeReference<List<Hotel>>() {});
                hotelList = FXCollections.observableArrayList(hotels);
            } else {
                hotelList = FXCollections.observableArrayList();
                statusLabel.setText("No se encontraron hoteles: " + response.getMessage());
            }
        } catch (Exception e) {
            hotelList = FXCollections.observableArrayList();
            statusLabel.setText("Error al cargar hoteles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRoomsForHotel(Hotel hotel) {
        try {
            String jsonResponse = roomData.readAll();
            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                List<Room> allRooms = objectMapper.convertValue(response.getData(),
                        new TypeReference<List<Room>>() {});

                // Filtrar habitaciones por hotel
                List<Room> hotelRooms = allRooms.stream()
                        .filter(room -> room.getHotel().getHotelId() == hotel.getHotelId())
                        .collect(java.util.stream.Collectors.toList());

                roomList = FXCollections.observableArrayList(hotelRooms);
                roomsTableView.setItems(roomList);
            } else {
                roomList = FXCollections.observableArrayList();
                roomsTableView.setItems(roomList);
            }
        } catch (Exception e) {
            roomList = FXCollections.observableArrayList();
            roomsTableView.setItems(roomList);
            statusLabel.setText("Error al cargar habitaciones: " + e.getMessage());
        }
    }

    @FXML
    private void handleHotelSelection(MouseEvent event) {
        selectedHotel = hotelListView.getSelectionModel().getSelectedItem();
        if (selectedHotel != null) {
            // Fill the fields with hotel data
            codeTextField.setText(String.valueOf(selectedHotel.getHotelId()));
            nameTextField.setText(selectedHotel.getName());
            locationTextField.setText(""); // Adaptar según tu modelo de Hotel
            addressTextArea.setText(selectedHotel.getAddress());
            phoneTextField.setText(""); // Adaptar según tu modelo de Hotel
            categoryComboBox.setValue("3 Estrellas"); // Valor por defecto
            descriptionTextArea.setText(""); // Adaptar según tu modelo de Hotel

            // Set services based on the hotel (demo settings)
            wifiCheckBox.setSelected(true);
            poolCheckBox.setSelected(false);
            gymCheckBox.setSelected(true);
            restaurantCheckBox.setSelected(true);
            parkingCheckBox.setSelected(true);

            // Load rooms for this hotel
            loadRoomsForHotel(selectedHotel);

            // Enable buttons
            editButton.setDisable(false);
            deleteButton.setDisable(false);
            manageRoomsButton.setDisable(false);
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchTextField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            hotelListView.setItems(hotelList);
        } else {
            try {
                // Usar el método de búsqueda por nombre de HotelData
                String jsonResponse = hotelData.findByName(searchText);
                DataResponse response = parseDataResponse(jsonResponse);

                if (response.isSuccess()) {
                    List<Hotel> filteredHotels = objectMapper.convertValue(response.getData(),
                            new TypeReference<List<Hotel>>() {});
                    hotelListView.setItems(FXCollections.observableArrayList(filteredHotels));
                } else {
                    hotelListView.setItems(FXCollections.observableArrayList());
                    statusLabel.setText("No se encontraron hoteles con ese nombre");
                }
            } catch (Exception e) {
                statusLabel.setText("Error en la búsqueda: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddHotel(ActionEvent event) {
        clearFields();
        setFieldsEnabled(true);
        editMode = false;

        // Set default values
        codeTextField.setText("[Automático]");
        categoryComboBox.setValue("3 Estrellas");

        saveButton.setDisable(false);
        cancelButton.setDisable(false);

        statusLabel.setText("Agregando nuevo hotel...");
    }

    @FXML
    private void handleEditHotel(ActionEvent event) {
        if (selectedHotel != null) {
            setFieldsEnabled(true);
            editMode = true;

            saveButton.setDisable(false);
            cancelButton.setDisable(false);

            statusLabel.setText("Editando hotel: " + selectedHotel.getName());
        }
    }

    @FXML
    private void handleDeleteHotel(ActionEvent event) {
        if (selectedHotel != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText(null);
            alert.setContentText("¿Está seguro que desea eliminar el hotel \"" + selectedHotel.getName() + "\"?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String jsonResponse = hotelData.delete(selectedHotel.getHotelId());
                    DataResponse response = parseDataResponse(jsonResponse);

                    if (response.isSuccess()) {
                        // Recargar la lista
                        loadHotelsFromFile();
                        hotelListView.setItems(hotelList);
                        clearFields();

                        statusLabel.setText("Hotel eliminado con éxito");

                        // Disable buttons
                        editButton.setDisable(true);
                        deleteButton.setDisable(true);
                        manageRoomsButton.setDisable(true);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "No se pudo eliminar el hotel: " + response.getMessage());
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Error al eliminar el hotel: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (validateFields()) {
            try {
                Hotel hotel;

                if (editMode) {
                    hotel = selectedHotel;
                } else {
                    hotel = new Hotel(getNextHotelId(), nameTextField.getText(),
                            addressTextArea.getText(), new ArrayList<>());
                }

                // Update the hotel with form data
                hotel.setName(nameTextField.getText());
                hotel.setAddress(addressTextArea.getText());

                String jsonResponse;
                if (editMode) {
                    jsonResponse = hotelData.update(hotel);
                } else {
                    jsonResponse = hotelData.create(hotel);
                }

                DataResponse response = parseDataResponse(jsonResponse);

                if (response.isSuccess()) {
                    // Recargar la lista
                    loadHotelsFromFile();
                    hotelListView.setItems(hotelList);

                    // Reset UI
                    setFieldsEnabled(false);
                    saveButton.setDisable(true);
                    cancelButton.setDisable(true);

                    // Seleccionar el hotel guardado
                    for (Hotel h : hotelList) {
                        if (h.getHotelId() == hotel.getHotelId()) {
                            hotelListView.getSelectionModel().select(h);
                            break;
                        }
                    }

                    statusLabel.setText("Hotel guardado con éxito");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "No se pudo guardar el hotel: " + response.getMessage());
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Error al guardar el hotel: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        if (editMode && selectedHotel != null) {
            // Reload current hotel data
            handleHotelSelection(null);
        } else {
            clearFields();
        }

        setFieldsEnabled(false);
        saveButton.setDisable(true);
        cancelButton.setDisable(true);

        statusLabel.setText("Operación cancelada");
    }

    @FXML
    private void handleManageRooms(ActionEvent event) {
        if (selectedHotel != null) {
            try {
                // Load the room management FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/room-management.fxml"));
                Parent root = loader.load();

                // Get the controller and pass the selected hotel
                RoomManagementController controller = loader.getController();
                controller.setSelectedHotel(selectedHotel);

                // Create a new stage for the room management window
                Stage roomStage = new Stage();
                roomStage.setTitle("Gestión de Habitaciones - " + selectedHotel.getName());
                roomStage.setScene(new Scene(root));
                roomStage.initModality(Modality.WINDOW_MODAL);
                roomStage.initOwner(manageRoomsButton.getScene().getWindow());
                roomStage.show();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "No se pudo cargar la ventana de gestión de habitaciones: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleClose(ActionEvent event) throws IOException {
        DataFactory.closeAll();

        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void clearFields() {
        codeTextField.clear();
        nameTextField.clear();
        locationTextField.clear();
        addressTextArea.clear();
        phoneTextField.clear();
        categoryComboBox.setValue(null);
        descriptionTextArea.clear();

        wifiCheckBox.setSelected(false);
        poolCheckBox.setSelected(false);
        gymCheckBox.setSelected(false);
        restaurantCheckBox.setSelected(false);
        parkingCheckBox.setSelected(false);

        roomsTableView.setItems(null);
    }

    private void setFieldsEnabled(boolean enabled) {
        nameTextField.setDisable(!enabled);
        locationTextField.setDisable(!enabled);
        addressTextArea.setDisable(!enabled);
        phoneTextField.setDisable(!enabled);
        categoryComboBox.setDisable(!enabled);
        descriptionTextArea.setDisable(!enabled);

        wifiCheckBox.setDisable(!enabled);
        poolCheckBox.setDisable(!enabled);
        gymCheckBox.setDisable(!enabled);
        restaurantCheckBox.setDisable(!enabled);
        parkingCheckBox.setDisable(!enabled);
    }

    private boolean validateFields() {
        String errorMessage = "";

        if (nameTextField.getText().trim().isEmpty()) {
            errorMessage += "El nombre del hotel no puede estar vacío.\n";
        }

        if (addressTextArea.getText().trim().isEmpty()) {
            errorMessage += "La dirección no puede estar vacía.\n";
        }

        if (!errorMessage.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Validación");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private int getNextHotelId() {
        int maxId = 0;
        for (Hotel hotel : hotelList) {
            if (hotel.getHotelId() > maxId) {
                maxId = hotel.getHotelId();
            }
        }
        return maxId + 1;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}