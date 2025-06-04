package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.shared.data.GuestData;
import com.MagicalStay.shared.data.HotelData;
import com.MagicalStay.shared.data.RoomData;
import com.MagicalStay.shared.domain.Guest;
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
import java.util.stream.Collectors;
import java.util.Collections;

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
    private TextArea addressTextArea;

    @FXML
    private TextField phoneTextField;

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
    private GuestData guestData;
    private ObjectMapper objectMapper;

    //TODO, adaptar la tableview de Guests

    @FXML
    private TableView guestsTableView;
    @FXML
    private TableColumn guestNationalityColumn;
    @FXML
    private TableColumn guestEmailColumn;
    @FXML
    private TableColumn guestNameColumn;
    @FXML
    private TableColumn guestPhoneNumberColumn;
    @FXML
    private TableColumn guestDniColumn;
    @FXML
    private TableColumn guestAddressColumn;
    @FXML
    private TableColumn guestLastNameColumn;
    @FXML
    private ComboBox searchTypeComboBox;
    @FXML
    private Button manageGuestsButton;

    @FXML
    private void initialize() {
        try {
            // Initialize data access objects
            hotelData = DataFactory.getHotelData();
            roomData = DataFactory.getRoomData();
            objectMapper = new ObjectMapper();

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

            guestNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            guestLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            guestDniColumn.setCellValueFactory(new PropertyValueFactory<>("dni"));
            guestEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            guestPhoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
            guestAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            guestNationalityColumn.setCellValueFactory(new PropertyValueFactory<>("nationality"));


            searchTypeComboBox.setItems(FXCollections.observableArrayList(
                "Por Nombre",
                "Por Dirección",
                "Por ID",
                "Todos"
            ));
            searchTypeComboBox.setValue("Por Nombre"); // valor por defecto

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Inicialización",
                    "No se pudieron cargar los datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadHotelsFromFile() {
        try {
            String jsonResponse = hotelData.retrieveAll();
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

    private void loadGuestsForHotel(Hotel hotel) {
        try {
            String jsonResponse = guestData.readAll();
            DataResponse response = parseDataResponse(jsonResponse);

           if (response.isSuccess()) {
                List<Guest> allGuests = objectMapper.convertValue(response.getData(),
                        new TypeReference<List<Guest>>() {});

                // Filtrar huéspedes por hotel usando sus reservas
                List<Guest> hotelGuests = allGuests.stream()
                        .filter(guest -> {
                            // Verificar si el huésped tiene reservas
                            if (guest.getBookings() != null) {
                                // Buscar si alguna reserva corresponde al hotel seleccionado
                                return guest.getBookings().stream()
                                        .anyMatch(booking ->
                                                booking.getHotel() != null &&
                                                        booking.getHotel().getHotelId() == hotel.getHotelId()
                                        );
                            }
                            return false;
                        })
                        .collect(Collectors.toList());

                ObservableList<Guest> guestList = FXCollections.observableArrayList(hotelGuests);
                guestsTableView.setItems(guestList);

                if (hotelGuests.isEmpty()) {
                    statusLabel.setText("No se encontraron huéspedes para este hotel");
                } else {
                    statusLabel.setText("Se encontraron " + hotelGuests.size() + " huéspedes");
                }
            } else {
                guestsTableView.setItems(FXCollections.observableArrayList());
                statusLabel.setText("Error al cargar huéspedes: " + response.getMessage());
            }
        } catch (Exception e) {
            guestsTableView.setItems(FXCollections.observableArrayList());
            statusLabel.setText("Error al cargar huéspedes: " + e.getMessage());
        }
    }


    @FXML
    private void handleHotelSelection(MouseEvent event) {
        selectedHotel = hotelListView.getSelectionModel().getSelectedItem();
        if (selectedHotel != null) {
            // Fill the fields with hotel data
            codeTextField.setText(String.valueOf(selectedHotel.getHotelId()));
            nameTextField.setText(selectedHotel.getName());
            addressTextArea.setText(selectedHotel.getAddress());
            phoneTextField.setText(""); // Adaptar según tu modelo de Hotel

            loadRoomsForHotel(selectedHotel);
            loadGuestsForHotel(selectedHotel);

            // Enable buttons
            editButton.setDisable(false);
            deleteButton.setDisable(false);
            manageRoomsButton.setDisable(false);
            manageGuestsButton.setDisable(false);

        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchTextField.getText().trim();
        String searchType = searchTypeComboBox.getValue().toString();
        
        try {
            String jsonResponse;
            
            if (searchText.isEmpty() || searchType.equals("Todos")) {
                jsonResponse = hotelData.retrieveAll();
            } else {
                switch (searchType) {
                    case "Por Nombre":
                        jsonResponse = hotelData.retrieveByName(searchText);
                        break;
                    case "Por Dirección":
                        jsonResponse = hotelData.retrieveByAddress(searchText);
                        break;
                    case "Por ID":
                        try {
                            int id = Integer.parseInt(searchText);
                            jsonResponse = hotelData.retrieveById(id);
                        } catch (NumberFormatException e) {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "El ID debe ser un número válido");
                            return;
                        }
                        break;
                    default:
                        jsonResponse = hotelData.retrieveAll();
                }
            }

            DataResponse response = parseDataResponse(jsonResponse);
            
            if (response.isSuccess()) {
                List<Hotel> hotels;
                if (response.getData() instanceof List) {
                    hotels = objectMapper.convertValue(response.getData(),
                            new TypeReference<List<Hotel>>() {});
                } else {
                    // Si es búsqueda por ID, convertimos el hotel único a una lista
                    Hotel hotel = objectMapper.convertValue(response.getData(), Hotel.class);
                    hotels = Collections.singletonList(hotel);
                }
                hotelListView.setItems(FXCollections.observableArrayList(hotels));
                statusLabel.setText("Búsqueda completada con éxito");
            } else {
                hotelListView.setItems(FXCollections.observableArrayList());
                statusLabel.setText("No se encontraron resultados: " + response.getMessage());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error en la búsqueda: " + e.getMessage());
            statusLabel.setText("Error en la búsqueda: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddHotel(ActionEvent event) {
        clearFields();
        setFieldsEnabled(true);
        editMode = false;

        // Set default values
        codeTextField.setText("[Automático]");

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
                    loadHotelsFromFile();
                    hotelListView.setItems(hotelList);

                    setFieldsEnabled(false);
                    saveButton.setDisable(true);
                    cancelButton.setDisable(true);

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
        addressTextArea.clear();
        phoneTextField.clear();
        roomsTableView.setItems(null);
    }

    private void setFieldsEnabled(boolean enabled) {
        nameTextField.setDisable(!enabled);
        addressTextArea.setDisable(!enabled);
        phoneTextField.setDisable(!enabled);

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

    @FXML
    public void handleManageGuests(ActionEvent actionEvent) {
        if (selectedHotel != null) {
            try {
                // Cargar el FXML de gestión de huéspedes
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/guest.fxml"));
                Parent root = loader.load();

                // Obtener el controlador y pasar el hotel seleccionado
                GuestManagementController controller = loader.getController();
                controller.setSelectedHotel(selectedHotel);

                // Crear una nueva ventana para la gestión de huéspedes
                Stage guestStage = new Stage();
                guestStage.setTitle("Gestión de Huéspedes - " + selectedHotel.getName());
                guestStage.setScene(new Scene(root));
                guestStage.initModality(Modality.WINDOW_MODAL);
                guestStage.initOwner(manageGuestsButton.getScene().getWindow());
                guestStage.show();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "No se pudo cargar la ventana de gestión de huéspedes: " + e.getMessage());
                e.printStackTrace();
            }
        }

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