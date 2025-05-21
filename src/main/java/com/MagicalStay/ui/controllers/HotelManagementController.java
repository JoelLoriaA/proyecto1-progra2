package com.MagicalStay.ui.controllers;

import com.MagicalStay.domain.Hotel;
import com.MagicalStay.domain.Room;
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
    private TableColumn<Room, Double> roomPriceColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Button closeButton;

    // Data
    private ObservableList<Hotel> hotelList;
    private ObservableList<Room> roomList;
    private Hotel selectedHotel;
    private boolean editMode = false;

    @FXML
    private void initialize() {
        // Initialize category combo box
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "1 Estrella", "2 Estrellas", "3 Estrellas", "4 Estrellas", "5 Estrellas"
        ));

        // Setup room table columns
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("style"));
        roomStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        roomPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Initialize with demo data (this would come from the server in the real app)
        loadDemoData();

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
                    setText(hotel.getName() + " (" + hotel.getLocation() + ")");
                }
            }
        });

        // Disable detail fields initially
        setFieldsEnabled(false);
    }

    private void loadDemoData() {
        // Create demo hotel list
        hotelList = FXCollections.observableArrayList();

        Hotel hotel1 = new Hotel();
        hotel1.setId(1);
        hotel1.setName("Hotel Paraíso");
        hotel1.setLocation("San José");
        hotel1.setAddress("Calle 5, Avenida Central");
        hotel1.setPhone("2222-3333");
        hotel1.setCategory("4 Estrellas");
        hotel1.setDescription("Un lujoso hotel en el centro de la ciudad con excelentes vistas.");

        Hotel hotel2 = new Hotel();
        hotel2.setId(2);
        hotel2.setName("Costa Rica Resort");
        hotel2.setLocation("Puntarenas");
        hotel2.setAddress("Playa Hermosa");
        hotel2.setPhone("2666-7777");
        hotel2.setCategory("5 Estrellas");
        hotel2.setDescription("Resort de playa con todas las comodidades para unas vacaciones inolvidables.");

        hotelList.add(hotel1);
        hotelList.add(hotel2);

        // Create demo room list for the first hotel
        roomList = FXCollections.observableArrayList();

        Room room1 = new Room();
        room1.setRoomNumber("101");
        room1.setStyle("Estándar");
        room1.setStatus("Disponible");
        room1.setPrice(80.0);

        Room room2 = new Room();
        room2.setRoomNumber("102");
        room2.setStyle("Deluxe");
        room2.setStatus("Ocupada");
        room2.setPrice(120.0);

        Room room3 = new Room();
        room3.setRoomNumber("201");
        room3.setStyle("Suite");
        room3.setStatus("Disponible");
        room3.setPrice(200.0);

        roomList.add(room1);
        roomList.add(room2);
        roomList.add(room3);
    }

    @FXML
    private void handleHotelSelection(MouseEvent event) {
        selectedHotel = hotelListView.getSelectionModel().getSelectedItem();
        if (selectedHotel != null) {
            // Fill the fields with hotel data
            codeTextField.setText(String.valueOf(selectedHotel.getId()));
            nameTextField.setText(selectedHotel.getName());
            locationTextField.setText(selectedHotel.getLocation());
            addressTextArea.setText(selectedHotel.getAddress());
            phoneTextField.setText(selectedHotel.getPhone());
            categoryComboBox.setValue(selectedHotel.getCategory());
            descriptionTextArea.setText(selectedHotel.getDescription());

            // Set services based on the hotel (demo settings)
            wifiCheckBox.setSelected(true);
            poolCheckBox.setSelected(selectedHotel.getId() == 2); // Only for the resort
            gymCheckBox.setSelected(true);
            restaurantCheckBox.setSelected(true);
            parkingCheckBox.setSelected(true);

            // Load rooms for this hotel
            roomsTableView.setItems(roomList); // In real app, load actual rooms for this hotel

            // Enable buttons
            editButton.setDisable(false);
            deleteButton.setDisable(false);
            manageRoomsButton.setDisable(false);
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchTextField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            hotelListView.setItems(hotelList);
        } else {
            ObservableList<Hotel> filteredList = FXCollections.observableArrayList();
            for (Hotel hotel : hotelList) {
                if (hotel.getName().toLowerCase().contains(searchText) ||
                        hotel.getLocation().toLowerCase().contains(searchText)) {
                    filteredList.add(hotel);
                }
            }
            hotelListView.setItems(filteredList);
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
                // Remove from list (in real app, this would call the server)
                hotelList.remove(selectedHotel);
                clearFields();

                statusLabel.setText("Hotel eliminado con éxito");

                // Disable buttons
                editButton.setDisable(true);
                deleteButton.setDisable(true);
                manageRoomsButton.setDisable(true);
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (validateFields()) {
            Hotel hotel;

            if (editMode) {
                hotel = selectedHotel;
            } else {
                hotel = new Hotel();
                hotel.setId(getNextHotelId()); // Generate new ID
            }

            // Update the hotel with form data
            hotel.setName(nameTextField.getText());
            hotel.setLocation(locationTextField.getText());
            hotel.setAddress(addressTextArea.getText());
            hotel.setPhone(phoneTextField.getText());
            hotel.setCategory(categoryComboBox.getValue());
            hotel.setDescription(descriptionTextArea.getText());

            // In a real app, services would be saved too

            if (!editMode) {
                hotelList.add(hotel);
            }

            // Reset UI
            setFieldsEnabled(false);
            saveButton.setDisable(true);
            cancelButton.setDisable(true);

            // Update the list view
            hotelListView.setItems(null);
            hotelListView.setItems(hotelList);
            hotelListView.getSelectionModel().select(hotel);

            statusLabel.setText("Hotel guardado con éxito");
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
                roomStage.initModality(Modality.WINDOW_MODAL); // Block interaction with the hotel window
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
    private void handleClose(ActionEvent event) {
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

        if (nameTextField.getText().isEmpty()) {
            errorMessage += "El nombre del hotel no puede estar vacío.\n";
        }

        if (locationTextField.getText().isEmpty()) {
            errorMessage += "La ubicación no puede estar vacía.\n";
        }

        if (phoneTextField.getText().isEmpty()) {
            errorMessage += "El teléfono no puede estar vacío.\n";
        }

        if (categoryComboBox.getValue() == null) {
            errorMessage += "Debe seleccionar una categoría.\n";
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
            if (hotel.getId() > maxId) {
                maxId = hotel.getId();
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
}