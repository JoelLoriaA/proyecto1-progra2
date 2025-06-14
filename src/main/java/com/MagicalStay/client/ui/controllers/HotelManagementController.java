package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.shared.data.GuestData;
import com.MagicalStay.shared.data.HotelData;
import com.MagicalStay.shared.data.RoomData;
import com.MagicalStay.shared.domain.Booking;
import com.MagicalStay.shared.domain.Guest;
import com.MagicalStay.shared.domain.Hotel;
import com.MagicalStay.shared.domain.Room;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HotelManagementController {
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
    @FXML
    private TextField hotelIdTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
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
    private ObservableList<Hotel> hotelList;
    private ObservableList<Room> roomList;
    private Hotel selectedHotel;
    private boolean editMode = false;
    private HotelData hotelData;
    private RoomData roomData;
    private GuestData guestData;
    private ObjectMapper objectMapper;
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
    private void initialize() {
        try {
            objectMapper = new ObjectMapper();
            hotelData = DataFactory.getHotelData();
            roomData = DataFactory.getRoomData();
            guestData = DataFactory.getGuestData();
            roomNumberColumn.setCellValueFactory(new PropertyValueFactory<Room, String>("roomNumber"));
            roomTypeColumn.setCellValueFactory(new PropertyValueFactory<Room, String>("roomType"));
            roomStatusColumn.setCellValueFactory(new PropertyValueFactory<Room, String>("roomCondition"));
            guestNameColumn.setCellValueFactory(new PropertyValueFactory<Guest, String>("name"));
            guestLastNameColumn.setCellValueFactory(new PropertyValueFactory<Guest, String>("lastName"));
            guestDniColumn.setCellValueFactory(new PropertyValueFactory<Guest, Integer>("id"));
            guestPhoneNumberColumn.setCellValueFactory(new PropertyValueFactory<Guest, Integer>("phoneNumber"));
            guestEmailColumn.setCellValueFactory(new PropertyValueFactory<Guest, String>("email"));
            guestAddressColumn.setCellValueFactory(new PropertyValueFactory<Guest, String>("address"));
            guestNationalityColumn.setCellValueFactory(new PropertyValueFactory<Guest, String>("nationality"));
            hotelList = FXCollections.observableArrayList();
            roomList = FXCollections.observableArrayList();
            searchTypeComboBox.setItems(FXCollections.observableArrayList(
                    "Por Nombre",
                    "Por Dirección",
                    "Todos"
            ));
            searchTypeComboBox.setValue("Por Nombre");
            loadHotelsFromFile();
            hotelListView.setItems(hotelList);
            hotelListView.setCellFactory(lv -> new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel hotel, boolean empty) {
                    super.updateItem(hotel, empty);
                    if (empty || hotel == null) {
                        setText(null);
                    } else {
                        setText(String.format("🏨 #%d - %s 📍 %s",
                                hotel.getHotelId(),
                                hotel.getName(),
                                hotel.getAddress()));
                    }
                }
            });
            setFieldsEnabled(false);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            hotelListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedHotel = newVal;
                    loadRoomsForHotel(newVal);
                    loadGuestsForHotel(newVal);
                    editButton.setDisable(false);
                    deleteButton.setDisable(false);
                }
            });
            statusLabel.setText("Sistema inicializado correctamente");
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
                hotelListView.setItems(hotelList);
            } else {
                hotelList = FXCollections.observableArrayList();
                hotelListView.setItems(hotelList);
                statusLabel.setText("No se encontraron hoteles: " + response.getMessage());
            }
        } catch (Exception e) {
            hotelList = FXCollections.observableArrayList();
            hotelListView.setItems(hotelList);
            statusLabel.setText("Error al cargar hoteles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRoomsForHotel(Hotel hotel) {
        try {
            if (hotel == null) {
                roomsTableView.setItems(FXCollections.observableArrayList());
                return;
            }
            List<Room> rooms = DataFactory.getRoomData().getRoomsByHotelId(hotel.getHotelId());
            ObservableList<Room> observableRooms = FXCollections.observableArrayList(rooms);
            roomsTableView.setItems(observableRooms);
            if (rooms.isEmpty()) {
                statusLabel.setText("No hay habitaciones registradas para este hotel");
            } else {
                statusLabel.setText(String.format("Se encontraron %d habitaciones", rooms.size()));
            }
        } catch (Exception e) {
            statusLabel.setText("Error al cargar habitaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadGuestsForHotel(Hotel hotel) {
        try {
            if (hotel == null) {
                guestsTableView.setItems(FXCollections.observableArrayList());
                return;
            }
            String bookingJson = DataFactory.getBookingData().retrieveAll();
            DataResponse bookingResponse = parseDataResponse(bookingJson);

            if (bookingResponse.isSuccess() && bookingResponse.getData() != null) {
                List<Booking> bookings = objectMapper.convertValue(
                    bookingResponse.getData(),
                    new TypeReference<List<Booking>>() {}
                );
                List<Guest> guests = bookings.stream()
                    .filter(b -> b.getHotel() != null && b.getHotel().getHotelId() == hotel.getHotelId())
                    .map(Booking::getGuest)
                    .filter(g -> g != null)
                    .distinct()
                    .collect(Collectors.toList());
                guestsTableView.setItems(FXCollections.observableArrayList(guests));
            } else {
                guestsTableView.setItems(FXCollections.observableArrayList());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error al cargar huéspedes asociados al hotel: " + e.getMessage());
        }
    }

    @FXML
    private void handleHotelSelection(MouseEvent event) {
        selectedHotel = hotelListView.getSelectionModel().getSelectedItem();
        if (selectedHotel != null) {
            hotelIdTextField.setText(String.valueOf(selectedHotel.getHotelId()));
            nameTextField.setText(selectedHotel.getName());
            addressTextArea.setText(selectedHotel.getAddress());
            loadRoomsForHotel(selectedHotel);
            loadGuestsForHotel(selectedHotel);
            editButton.setDisable(false);
            deleteButton.setDisable(false);
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
        hotelIdTextField.setText("[Automático]");
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
                        loadHotelsFromFile();
                        hotelListView.setItems(hotelList);
                        clearFields();
                        statusLabel.setText("Hotel eliminado con éxito");
                        editButton.setDisable(true);
                        deleteButton.setDisable(true);
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
                List<Room> existingRooms = new ArrayList<>();

                if (editMode) {
                    hotel = selectedHotel;
                    existingRooms = selectedHotel.getRooms();
                } else {
                    hotel = new Hotel(getNextHotelId(), nameTextField.getText(),
                            addressTextArea.getText(), new ArrayList<>());
                }

                hotel.setName(nameTextField.getText());
                hotel.setAddress(addressTextArea.getText());
                if (!existingRooms.isEmpty()) {
                    hotel.setRooms(existingRooms);
                }

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
    private void handleClose(ActionEvent event) throws IOException {
        DataFactory.closeAll();
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void clearFields() {
        hotelIdTextField.clear();
        nameTextField.clear();
        addressTextArea.clear();
        roomsTableView.setItems(null);
    }

    private void setFieldsEnabled(boolean enabled) {
        nameTextField.setDisable(!enabled);
        addressTextArea.setDisable(!enabled);
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
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}
