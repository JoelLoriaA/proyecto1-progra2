package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.shared.data.BookingData;
import com.MagicalStay.shared.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BookingManagementController {
    private static final double TAX_RATE = 0.12;
    private static final double SERVICE_FEE = 10.0;

    @FXML private TextField totalPriceTextField;
    @FXML private TableView<Room> reservedRoomsTableView;
    @FXML private DatePicker leavingDatePicker;
    @FXML private ComboBox<Hotel> hotelComboBox;
    @FXML private ComboBox<FrontDeskClerk> clerkComboBox;
    @FXML private ComboBox<Room> availableRoomsComboBox;
    @FXML private TextField bookingIdTextField;
    @FXML private TextField totalNightsTextField;
    @FXML private TextField subtotalTextField;
    @FXML private DatePicker startDatePicker;
    @FXML private ComboBox<Guest> guestComboBox;

    @FXML private TableColumn<Room, String> reservedRoomNumberColumn;
    @FXML private TableColumn<Room, RoomType> reservedRoomTypeColumn;
    @FXML private TableColumn<Room, RoomCondition> reservedRoomConditionColumn;
    @FXML private TableColumn<Room, Double> reservedRoomPrice;

    @FXML private Button closeButton;
    @FXML private Button saveButton;
    @FXML private Button addRoomButton;
    @FXML private Label statusLabel;
    @FXML private Button checkAvailabilityButton;
    @FXML private Button deleteButton;
    @FXML private TextField searchTextField;
    @FXML private ComboBox<String> searchTypeComboBox;
    @FXML private Button addButton;
    @FXML private Button editButton;

    private BookingData bookingData;
    private final ObservableList<Room> reservedRooms = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookings = FXCollections.observableArrayList();
    private boolean isEditing = false;
    private ObjectMapper objectMapper;
    @FXML
    private ListView bookingListView;

    @FXML
    public void initialize() {
        try {
            bookingData = DataFactory.getBookingData();
            objectMapper = new ObjectMapper();
            setupControls();
            loadInitialData();
        } catch (IOException e) {
            showError("Error al inicializar: " + e.getMessage());
        }
    }

    private void setupControls() {
        setupSearchControls();
        setupDatePickers();
        setupTableColumns();
        setupComboBoxes();
        bindControls();
        setupListView();

        reservedRoomsTableView.setItems(reservedRooms);
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateCalculations());
        leavingDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateCalculations());
    }

    private void setupSearchControls() {
        searchTypeComboBox.setItems(FXCollections.observableArrayList(
                "ID", "Huésped", "Hotel" , "Todos"
        ));
        searchTypeComboBox.setValue("ID");
    }

    private void setupDatePickers() {
        startDatePicker.setValue(LocalDate.now());
        leavingDatePicker.setValue(LocalDate.now().plusDays(1));
        startDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    private void setupTableColumns() {
        reservedRoomNumberColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRoomNumber()));
        reservedRoomTypeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRoomType()));
        reservedRoomConditionColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRoomCondition()));
        reservedRoomPrice.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
    }

    private void setupComboBoxes() {
        try {
            String hotelResponse = DataFactory.getHotelData().retrieveAll();
            String guestResponse = DataFactory.getGuestData().readAll();
            String clerkResponse = DataFactory.getFrontDeskData().readAll();

            DataResponse hotelDataResponse = parseDataResponse(hotelResponse);
            DataResponse guestDataResponse = parseDataResponse(guestResponse);
            DataResponse clerkDataResponse = parseDataResponse(clerkResponse);

            if (hotelDataResponse.isSuccess()) {
                List<Hotel> hotels = objectMapper.convertValue(hotelDataResponse.getData(),
                        new TypeReference<List<Hotel>>() {});
                hotelComboBox.setItems(FXCollections.observableArrayList(hotels));
            }

            if (guestDataResponse.isSuccess()) {
                List<Guest> guests = objectMapper.convertValue(guestDataResponse.getData(),
                        new TypeReference<List<Guest>>() {});
                guestComboBox.setItems(FXCollections.observableArrayList(guests));
            }

            if (clerkDataResponse.isSuccess()) {
                List<FrontDeskClerk> clerks = objectMapper.convertValue(clerkDataResponse.getData(),
                        new TypeReference<List<FrontDeskClerk>>() {});
                clerkComboBox.setItems(FXCollections.observableArrayList(clerks));
            }
        } catch (Exception e) {
            showError("Error al cargar datos: " + e.getMessage());
        }
    }

    private void setupListView() {
        bookingListView.setItems(bookings);
        bookingListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) loadBookingDetails((Booking) newVal);
                });
    }

    private void bindControls() {
        editButton.disableProperty().bind(bookingListView.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(bookingListView.getSelectionModel().selectedItemProperty().isNull());
        addRoomButton.disableProperty().bind(availableRoomsComboBox.valueProperty().isNull());
        saveButton.disableProperty().bind(
                hotelComboBox.valueProperty().isNull()
                        .or(guestComboBox.valueProperty().isNull())
                        .or(clerkComboBox.valueProperty().isNull())
        );
    }

    private void loadInitialData() {
        try {
            String jsonResponse = bookingData.retrieveAll();
            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                List<Booking> bookingList = objectMapper.convertValue(response.getData(),
                        new TypeReference<List<Booking>>() {});
                bookings.setAll(bookingList);
            } else {
                bookings.clear();
                showError("No se encontraron reservas: " + response.getMessage());
            }
        } catch (Exception e) {
            bookings.clear();
            showError("Error al cargar reservas: " + e.getMessage());
        }
    }

    @FXML
    public void handleCheckAvailability(ActionEvent event) {
        if (!validateDates()) return;
        updateAvailableRooms();
    }

    private boolean validateDates() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate leavingDate = leavingDatePicker.getValue();

        if (startDate == null || leavingDate == null) {
            showError("Las fechas son obligatorias");
            return false;
        }

        if (leavingDate.isBefore(startDate) || leavingDate.equals(startDate)) {
            showError("La fecha de salida debe ser posterior a la fecha de entrada");
            return false;
        }

        return true;
    }

    private void updateAvailableRooms() {
        if (hotelComboBox.getValue() == null) return;

        LocalDate startDate = startDatePicker.getValue();
        LocalDate leavingDate = leavingDatePicker.getValue();

        List<Room> allRooms = hotelComboBox.getValue().getRooms();
        List<Room> availableRooms = findAvailableRooms(allRooms, startDate, leavingDate);

        availableRoomsComboBox.setItems(FXCollections.observableArrayList(availableRooms));
    }

    private List<Room> findAvailableRooms(List<Room> allRooms, LocalDate startDate, LocalDate leavingDate) {

        try {
                String jsonResponse = bookingData.retrieveAll();
                DataResponse response = parseDataResponse(jsonResponse);
                if (response.isSuccess()) {
                    List<Booking> existingBookings = objectMapper.convertValue(response.getData(),
                            new TypeReference<List<Booking>>() {});

                    return allRooms.stream()
                            .filter(room -> isRoomAvailable(room, startDate, leavingDate, existingBookings))
                            .collect(Collectors.toList());}
                return new ArrayList<>();
        } catch (Exception e) {
                showError("Error al verificar disponibilidad: " + e.getMessage());
                return new ArrayList<>();
        }
    }

    private boolean isRoomAvailable(Room room, LocalDate startDate, LocalDate leavingDate, List<Booking> existingBookings) {
        return existingBookings.stream()
                .noneMatch(booking ->
                        booking.getReservedRooms().contains(room) &&
                                (
                                        (startDate.isBefore(booking.getLeavingDate()) || startDate.isEqual(booking.getLeavingDate())) &&
                                                (leavingDate.isAfter(booking.getStartDate()) || leavingDate.isEqual(booking.getStartDate()))
                                )
                );
    }

    @FXML
    public void handleAddRoom(ActionEvent event) {
        Room selectedRoom = availableRoomsComboBox.getValue();
        if (selectedRoom != null && !reservedRooms.contains(selectedRoom)) {
            reservedRooms.add(selectedRoom);
            updateCalculations();
        }
    }

    @Deprecated
    public void handleRemoveRoom(ActionEvent event) {
        Room selectedRoom = reservedRoomsTableView.getSelectionModel().getSelectedItem();
        if (selectedRoom != null) {
            reservedRooms.remove(selectedRoom);
            updateCalculations();
        }
    }

    private void updateCalculations() {
        if (!validateDates()) return;

        LocalDate startDate = startDatePicker.getValue();
        LocalDate leavingDate = leavingDatePicker.getValue();
        long nights = ChronoUnit.DAYS.between(startDate, leavingDate);

        totalNightsTextField.setText(String.valueOf(nights));

        double subtotal = calculateSubtotal(nights);
        double total = calculateTotal(subtotal);

        subtotalTextField.setText(String.format("%.2f", subtotal));
        totalPriceTextField.setText(String.format("%.2f", total));
    }

    private double calculateSubtotal(long nights) {
        return reservedRooms.stream()
                .mapToDouble(room -> room.getPrice() * nights)
                .sum();
    }

    private double calculateTotal(double subtotal) {
        double tax = subtotal * TAX_RATE;
        return subtotal + tax + SERVICE_FEE;
    }

    @Deprecated
    public void handleSearch(ActionEvent event) {
        String searchType = searchTypeComboBox.getValue();
        String searchText = searchTextField.getText().trim();

        try {
            String jsonResponse;

            if (searchText.isEmpty()) {
                jsonResponse = bookingData.retrieveAll();
            } else {
                switch (searchType) {
                    case "ID":
                        try {
                            int id = Integer.parseInt(searchText);
                            jsonResponse = bookingData.retrieveById(id);
                        } catch (NumberFormatException e) {
                            showError("El ID debe ser un número válido");
                            return;
                        }
                        break;
                    case "Huésped":
                        jsonResponse = bookingData.retrieveByGuest(searchText);
                        break;
                    case "Hotel":
                        jsonResponse = bookingData.retrieveByHotel(searchText);
                        break;
                    default:
                        jsonResponse = bookingData.retrieveAll();
                }
            }

            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                List<Booking> foundBookings;
                if (response.getData() instanceof List) {
                    foundBookings = objectMapper.convertValue(response.getData(),
                            new TypeReference<List<Booking>>() {});
                } else {
                    Booking booking = objectMapper.convertValue(response.getData(), Booking.class);
                    foundBookings = Collections.singletonList(booking);
                }
                bookings.setAll(foundBookings);
                showSuccess("Búsqueda completada con éxito");
            } else {
                bookings.clear();
                showError("No se encontraron resultados: " + response.getMessage());
            }
        } catch (Exception e) {
            showError("Error en la búsqueda: " + e.getMessage());
        }
    }

    private boolean matchesSearch(Booking booking, String searchType, String searchText) {
        switch (searchType) {
            case "ID":
                return String.valueOf(booking.getBookingId()).contains(searchText);
            case "Huésped":
                return booking.getGuest().getName().toLowerCase().contains(searchText);
            case "Hotel":
                return booking.getHotel().getName().toLowerCase().contains(searchText);
            case "Fecha":
                return booking.getStartDate().toString().contains(searchText) ||
                        booking.getLeavingDate().toString().contains(searchText);
            default:
                return false;
        }
    }

    @FXML
    public void handleAddReservation(ActionEvent event){
            clearForm();
            isEditing = false;
            try {
                String jsonResponse = bookingData.retrieveAll();
                DataResponse response = parseDataResponse(jsonResponse);

                if (response.isSuccess()) {
                    List<Booking> bookingList = objectMapper.convertValue(response.getData(),
                            new TypeReference<List<Booking>>() {});
                    int newId = bookingList.size() + 1;
                    bookingIdTextField.setText(String.valueOf(newId));
                } else {
                    bookingIdTextField.setText("1");
                }
            } catch (Exception e) {
                showError("Error al generar nuevo ID: " + e.getMessage());
                bookingIdTextField.setText("1");
            }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (!validateBooking()) return;

        try {
            Booking booking = createBooking();
            String result = isEditing ?
                    bookingData.update(booking) :
                    bookingData.create(booking);

            if (result != null) {
                showSuccess("Reserva " + (isEditing ? "actualizada" : "creada") + " exitosamente");
                loadInitialData();
                clearForm();
            }
        } catch (Exception e) {
            showError("Error al guardar la reserva: " + e.getMessage());
        }
    }

    private boolean validateBooking() {
        if (reservedRooms.isEmpty()) {
            showError("Debe seleccionar al menos una habitación");
            return false;
        }
        if (guestComboBox.getValue() == null) {
            showError("Debe seleccionar un huésped");
            return false;
        }
        if (clerkComboBox.getValue() == null) {
            showError("Debe seleccionar un empleado");
            return false;
        }
        return validateDates();
    }

    private Booking createBooking() {
        int bookingId = Integer.parseInt(bookingIdTextField.getText());
        Booking booking = new Booking(
                bookingId,
                startDatePicker.getValue(),
                leavingDatePicker.getValue(),
                new ArrayList<>(reservedRooms)
        );
        booking.setGuest(guestComboBox.getValue());
        booking.setFrontDeskClerk(clerkComboBox.getValue());
        booking.setHotel(hotelComboBox.getValue());
        return booking;
    }

    private void loadBookingDetails(Booking booking) {
        if (booking == null) return;

        isEditing = true;
        bookingIdTextField.setText(String.valueOf(booking.getBookingId()));
        hotelComboBox.setValue(booking.getHotel());
        startDatePicker.setValue(booking.getStartDate());
        leavingDatePicker.setValue(booking.getLeavingDate());
        guestComboBox.setValue(booking.getGuest());
        clerkComboBox.setValue(booking.getFrontDeskClerk());
        reservedRooms.setAll(booking.getReservedRooms());
        updateCalculations();
    }

    @FXML
    public void handleClear(ActionEvent event) {
        clearForm();
    }

    private void clearForm() {
        isEditing = false;
        bookingIdTextField.clear();
        startDatePicker.setValue(LocalDate.now());
        leavingDatePicker.setValue(LocalDate.now().plusDays(1));
        hotelComboBox.setValue(null);
        guestComboBox.setValue(null);
        clerkComboBox.setValue(null);
        availableRoomsComboBox.setValue(null);
        reservedRooms.clear();
        updateCalculations();
    }

    @FXML
    public void handleClose(ActionEvent event) {
        ((Stage) closeButton.getScene().getWindow()).close();
    }

    private void showError(String message) {
        statusLabel.setText("❌ " + message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        statusLabel.setText("✅ " + message);
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    private DataResponse parseDataResponse(String jsonResponse) throws IOException {
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