// src/main/java/com/MagicalStay/client/ui/controllers/BookingManagementController.java
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
                        import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
                        import com.fasterxml.jackson.databind.SerializationFeature;

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
                            @FXML private ListView bookingListView;
                            @FXML private Button updateBookingButton;

                            private BookingData bookingData;
                            private final ObservableList<Room> reservedRooms = FXCollections.observableArrayList();
                            private final ObservableList<Booking> bookings = FXCollections.observableArrayList();
                            private boolean isEditing = false;
                            private ObjectMapper objectMapper;

                            @FXML
                            public void initialize() {
                                try {
                                    bookingData = DataFactory.getBookingData();
                                    objectMapper = new ObjectMapper();
                                    objectMapper.registerModule(new JavaTimeModule());
                                    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                                    setupControls();
                                    loadInitialData();
                                    setFieldsEnabled(false);
                                    availableRoomsComboBox.setDisable(true);
                                } catch (Exception e) {
                                    showError("Error al inicializar: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }private <T> void selectComboBoxById(ComboBox<T> comboBox, T target, java.util.function.Predicate<T> matcher) {
                                if (target == null) {
                                    comboBox.setValue(null);
                                    return;
                                }
                                for (T item : comboBox.getItems()) {
                                    if (matcher.test(item)) {
                                        comboBox.setValue(item);
                                        return;
                                    }
                                }
                                comboBox.setValue(null);
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
                                        new javafx.beans.property.SimpleStringProperty(
                                                cellData.getValue().getRoomNumber()));

                                reservedRoomTypeColumn.setCellValueFactory(cellData ->
                                        new javafx.beans.property.SimpleObjectProperty<>(
                                                cellData.getValue().getRoomType()));

                                reservedRoomConditionColumn.setCellValueFactory(cellData ->
                                        new javafx.beans.property.SimpleObjectProperty<>(
                                                cellData.getValue().getRoomCondition()));

                                reservedRoomPrice.setCellValueFactory(cellData ->
                                        new javafx.beans.property.SimpleObjectProperty<>(
                                                cellData.getValue().getPrice()));

                                reservedRoomsTableView.setItems(reservedRooms);
                                reservedRoomsTableView.getSelectionModel().selectedItemProperty().addListener(
                                        (obs, oldSelection, newSelection) -> {
                                            deleteButton.setDisable(newSelection == null || !isEditing);
                                        });
                            }

                            private void setupComboBoxes() {
                                try {
                                    String hotelResponse = DataFactory.getHotelData().retrieveAll();
                                    String guestResponse = DataFactory.getGuestData().retrieveAll();
                                    String clerkResponse = DataFactory.getFrontDeskData().retrieveAll();

                                    DataResponse hotelDataResponse = parseDataResponse(hotelResponse);
                                    DataResponse guestDataResponse = parseDataResponse(guestResponse);
                                    DataResponse clerkDataResponse = parseDataResponse(clerkResponse);

                                    if (hotelDataResponse.isSuccess()) {
                                        List<Hotel> hotels = objectMapper.convertValue(hotelDataResponse.getData(),
                                                new TypeReference<List<Hotel>>() {});

                                        hotelComboBox.setItems(FXCollections.observableArrayList(hotels));
                                        hotelComboBox.setCellFactory(param -> new ListCell<Hotel>() {
                                            @Override
                                            protected void updateItem(Hotel hotel, boolean empty) {
                                                super.updateItem(hotel, empty);
                                                setText(empty || hotel == null ? null : hotel.getName());
                                            }
                                        });
                                        hotelComboBox.setButtonCell(hotelComboBox.getCellFactory().call(null));
                                        hotelComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                                            if (newVal != null) {
                                                availableRoomsComboBox.setDisable(true);
                                                availableRoomsComboBox.getItems().clear();
                                            }
                                        });
                                    }

                                    if (guestDataResponse.isSuccess()) {
                                        List<Guest> guests = objectMapper.convertValue(guestDataResponse.getData(),
                                                new TypeReference<List<Guest>>() {});
                                        guestComboBox.setItems(FXCollections.observableArrayList(guests));
                                        guestComboBox.setCellFactory(param -> new ListCell<Guest>() {
                                            @Override
                                            protected void updateItem(Guest guest, boolean empty) {
                                                super.updateItem(guest, empty);
                                                setText(empty || guest == null ? null : guest.getName() + " " + guest.getLastName());
                                            }
                                        });
                                        guestComboBox.setButtonCell(guestComboBox.getCellFactory().call(null));
                                    }

                                    if (clerkDataResponse.isSuccess()) {
                                        List<FrontDeskClerk> clerks = objectMapper.convertValue(clerkDataResponse.getData(),
                                                new TypeReference<List<FrontDeskClerk>>() {});
                                        clerkComboBox.setItems(FXCollections.observableArrayList(clerks));
                                        clerkComboBox.setCellFactory(param -> new ListCell<FrontDeskClerk>() {
                                            @Override
                                            protected void updateItem(FrontDeskClerk clerk, boolean empty) {
                                                super.updateItem(clerk, empty);
                                                setText(empty || clerk == null ? null : clerk.getName() + " " + clerk.getLastNames());
                                            }
                                        });
                                        clerkComboBox.setButtonCell(clerkComboBox.getCellFactory().call(null));
                                    }

                                    availableRoomsComboBox.setCellFactory(param -> new ListCell<Room>() {
                                        @Override
                                        protected void updateItem(Room room, boolean empty) {
                                            super.updateItem(room, empty);
                                            setText(empty || room == null ? null :
                                                    String.format("Hab. %s - %s ($%.2f)", room.getRoomNumber(), room.getRoomType(), room.getPrice()));
                                        }
                                    });
                                    availableRoomsComboBox.setButtonCell(availableRoomsComboBox.getCellFactory().call(null));
                                    availableRoomsComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                                            addRoomButton.setDisable(newVal == null));
                                    availableRoomsComboBox.setDisable(true);
                                } catch (Exception e) {
                                    showError("Error al cargar datos: " + e.getMessage());
                                }
                            }

                            private void setupListView() {
                                bookingListView.setCellFactory(lv -> new ListCell<Booking>() {
                                    @Override
                                    protected void updateItem(Booking booking, boolean empty) {
                                        super.updateItem(booking, empty);
                                        if (empty || booking == null) {
                                            setText(null);
                                        } else {
                                            String guestName = booking.getGuest() != null ?
                                                    booking.getGuest().getName() + " " + booking.getGuest().getLastName() : "N/A";
                                            String hotelName = booking.getHotel() != null ?
                                                    booking.getHotel().getName() : "N/A";
                                            setText(String.format("📅 #%d - (%s → %s) | 🏨 %s | 👤 %s",
                                                    booking.getBookingId(),
                                                    booking.getStartDate(),
                                                    booking.getLeavingDate(),
                                                    hotelName,
                                                    guestName
                                            ));
                                        }
                                    }
                                });

                                bookingListView.getSelectionModel().selectedItemProperty().addListener(
                                    (obs, oldVal, newVal) -> {
                                        if (newVal instanceof Booking) {
                                            loadBookingDetails((Booking) newVal);
                                        } else {
                                            clearForm();
                                        }
                                    }
                                );
                                bookingListView.setItems(bookings);
                            }

                            private void bindControls() {
                                editButton.disableProperty().unbind();
                                deleteButton.disableProperty().unbind();
                                editButton.disableProperty().bind(bookingListView.getSelectionModel().selectedItemProperty().isNull());
                                deleteButton.disableProperty().bind(bookingListView.getSelectionModel().selectedItemProperty().isNull());
                            }

                            private void unbindControls() {
                                editButton.disableProperty().unbind();
                                deleteButton.disableProperty().unbind();
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
                                availableRoomsComboBox.setDisable(false);
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
                                try {
                                    List<Room> hotelRooms = DataFactory.getRoomData()
                                            .getRoomsByHotelId(hotelComboBox.getValue().getHotelId());
                                    if (validateDates()) {
                                        LocalDate startDate = startDatePicker.getValue();
                                        LocalDate leavingDate = leavingDatePicker.getValue();
                                        List<Room> availableRooms = findAvailableRooms(hotelRooms, startDate, leavingDate);
                                        availableRoomsComboBox.setItems(FXCollections.observableArrayList(availableRooms));
                                    }
                                } catch (Exception e) {
                                    showError("Error al actualizar habitaciones disponibles: " + e.getMessage());
                                }
                            }

                            private List<Room> findAvailableRooms(List<Room> allRooms, LocalDate startDate, LocalDate leavingDate) {
                                try {
                                    String jsonResponse = bookingData.retrieveAll();
                                    DataResponse response = parseDataResponse(jsonResponse);
                                    if (response.isSuccess()) {
                                        List<Booking> existingBookings = objectMapper.convertValue(response.getData(),
                                                new TypeReference<List<Booking>>() {});
                                        List<Booking> bookingsToCheck = existingBookings;
                                        if (isEditing && bookingIdTextField.getText() != null && !bookingIdTextField.getText().isEmpty()) {
                                            try {
                                                int currentId = Integer.parseInt(bookingIdTextField.getText());
                                                List<Booking> filtered = existingBookings.stream()
                                                        .filter(b -> b.getBookingId() != currentId)
                                                        .collect(Collectors.toList());
                                                bookingsToCheck = filtered;
                                            } catch (NumberFormatException ignored) {}
                                        }
                                        final List<Booking> finalBookingsToCheck = bookingsToCheck;
                                        return allRooms.stream()
                                                .filter(room -> isRoomAvailable(room, startDate, leavingDate, finalBookingsToCheck))
                                                .collect(Collectors.toList());
                                    }
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
                                if (selectedRoom != null) {
                                    boolean roomAlreadyAdded = reservedRooms.stream()
                                            .anyMatch(room -> room.getRoomNumber().equals(selectedRoom.getRoomNumber()));
                                    if (!roomAlreadyAdded) {
                                        reservedRooms.add(selectedRoom);
                                        availableRoomsComboBox.setValue(null);
                                        updateCalculations();
                                        showSuccess("Habitación " + selectedRoom.getRoomNumber() + " agregada");
                                    } else {
                                        showError("Esta habitación ya está en la lista");
                                    }
                                } else {
                                    showError("Por favor seleccione una habitación");
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

                            @FXML
                            public void handleSearch(ActionEvent event) {
                                String searchType = searchTypeComboBox.getValue();
                                String searchText = searchTextField.getText().trim();
                                try {
                                    String jsonResponse;
                                    if (searchText.isEmpty() || searchType.equals("Todos")) {
                                        jsonResponse = bookingData.retrieveAll();
                                    } else if (searchType.equals("ID")) {
                                        try {
                                            int id = Integer.parseInt(searchText);
                                            jsonResponse = bookingData.retrieveById(id);
                                        } catch (NumberFormatException e) {
                                            showError("El ID debe ser un número válido");
                                            return;
                                        }
                                    } else {
                                        jsonResponse = bookingData.retrieveAll();
                                        DataResponse response = parseDataResponse(jsonResponse);
                                        if (response.isSuccess() && response.getData() != null) {
                                            List<Booking> allBookings = objectMapper.convertValue(response.getData(),
                                                new TypeReference<List<Booking>>() {});
                                            List<Booking> filteredBookings = allBookings.stream()
                                                .filter(booking -> matchesSearch(booking, searchType, searchText))
                                                .collect(Collectors.toList());
                                            if (!filteredBookings.isEmpty()) {
                                                bookings.setAll(filteredBookings);
                                                showSuccess("Búsqueda completada con éxito");
                                            } else {
                                                bookings.clear();
                                                showError("No se encontraron resultados");
                                            }
                                            return;
                                        }
                                    }
                                    DataResponse response = parseDataResponse(jsonResponse);
                                    if (response.isSuccess() && response.getData() != null) {
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
                                        showError("No se encontraron resultados");
                                    }
                                } catch (Exception e) {
                                    showError("Error en la búsqueda: " + e.getMessage());
                                }
                            }

                            private boolean matchesSearch(Booking booking, String searchType, String searchText) {
                                switch (searchType) {
                                    case "Huésped":
                                        return booking.getGuest() != null &&
                                               booking.getGuest().getName().toLowerCase()
                                               .contains(searchText.toLowerCase());
                                    case "Hotel":
                                        return booking.getHotel() != null &&
                                               booking.getHotel().getName().toLowerCase()
                                               .contains(searchText.toLowerCase());
                                    default:
                                        return false;
                                }
                            }

                            @FXML
                            public void handleAddReservation(ActionEvent event) {
                                clearForm();
                                isEditing = false;
                                setFieldsEnabled(true);
                                try {
                                    int nextId = DataFactory.getBookingData().getNextBookingId();
                                    bookingIdTextField.setText("[Automático]");
                                    bookingIdTextField.setDisable(true);
                                    saveButton.setDisable(false);
                                } catch (IOException e) {
                                    showError("Error al generar ID de reserva: " + e.getMessage());
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
                                    DataResponse response = parseDataResponse(result);
                                    if (response.isSuccess()) {
                                        showSuccess("Reserva " + (isEditing ? "actualizada" : "creada") + " exitosamente");
                                        loadInitialData();
                                        clearForm();
                                        bookingListView.getItems().stream()
                                            .filter(b -> b instanceof Booking &&
                                                    ((Booking)b).getBookingId() == booking.getBookingId())
                                            .findFirst()
                                            .ifPresent(b -> {
                                                bookingListView.getSelectionModel().select(b);
                                            });
                                    } else {
                                        showError("Error al guardar: " + response.getMessage());
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

                            private Booking createBooking() throws IOException {
                                int bookingId;
                                if (isEditing && bookingIdTextField.getText() != null && !bookingIdTextField.getText().equals("[Automático]")) {
                                    bookingId = Integer.parseInt(bookingIdTextField.getText());
                                } else {
                                    bookingId = DataFactory.getBookingData().getNextBookingId();
                                }
                                Booking booking = new Booking(
                                        bookingId,
                                        startDatePicker.getValue(),
                                        leavingDatePicker.getValue(),
                                        new ArrayList<>(reservedRooms)
                                );
                                // Asigna el objeto completo seleccionado
                                booking.setGuest(guestComboBox.getValue());
                                booking.setFrontDeskClerk(clerkComboBox.getValue());
                                booking.setHotel(hotelComboBox.getValue());
                                return booking;
                            }

                            private void loadBookingDetails(Booking booking) {
                                if (booking == null) return;
                                try {
                                    unbindControls();
                                    bookingIdTextField.setText(String.valueOf(booking.getBookingId()));

                                    // Selecciona el hotel por ID
                                    selectComboBoxById(hotelComboBox, booking.getHotel(),
                                            h -> h != null && booking.getHotel() != null && ((Hotel)h).getHotelId() == booking.getHotel().getHotelId());

                                    // Selecciona el huésped por nombre y apellido
                                    selectComboBoxById(guestComboBox, booking.getGuest(),
                                            g -> g != null && booking.getGuest() != null &&
                                                    ((Guest)g).getName().equals(booking.getGuest().getName()) &&
                                                    ((Guest)g).getLastName().equals(booking.getGuest().getLastName()));

                                    // Selecciona el recepcionista por employeeId
                                    selectComboBoxById(clerkComboBox, booking.getFrontDeskClerk(),
                                            c -> c != null && booking.getFrontDeskClerk() != null &&
                                                    ((FrontDeskClerk)c).getEmployeeId().equals(booking.getFrontDeskClerk().getEmployeeId()));

                                    startDatePicker.setValue(booking.getStartDate());
                                    leavingDatePicker.setValue(booking.getLeavingDate());

                                    reservedRooms.setAll(booking.getReservedRooms() != null ? booking.getReservedRooms() : new ArrayList<>());
                                    reservedRoomsTableView.refresh();

                                    updateCalculations();
                                    availableRoomsComboBox.setDisable(true);
                                    setFieldsEnabled(false);
                                    editButton.setDisable(false);
                                    deleteButton.setDisable(false);
                                    bindControls();
                                    showSuccess("Reserva #" + booking.getBookingId() + " cargada");
                                } catch (Exception e) {
                                    showError("Error al cargar los detalles: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }

                            @FXML
                            public void handleClear(ActionEvent event) {
                                clearForm();
                            }

                            private void clearForm() {
                                unbindControls();
                                isEditing = false;
                                bookingIdTextField.clear();
                                startDatePicker.setValue(LocalDate.now());
                                leavingDatePicker.setValue(LocalDate.now().plusDays(1));
                                hotelComboBox.setValue(null);
                                guestComboBox.setValue(null);
                                clerkComboBox.setValue(null);
                                availableRoomsComboBox.setValue(null);
                                availableRoomsComboBox.setDisable(true);
                                reservedRooms.clear();
                                updateCalculations();
                                setFieldsEnabled(false);
                                bindControls();
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

                            @FXML
                            public void handleDeleteReservation(ActionEvent event) {
                                Booking selectedBooking = (Booking) bookingListView.getSelectionModel().getSelectedItem();
                                if (selectedBooking != null) {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                    alert.setTitle("Confirmar Eliminación");
                                    alert.setHeaderText(null);
                                    alert.setContentText("¿Está seguro que desea eliminar la reserva #" + selectedBooking.getBookingId() + "?");
                                    if (alert.showAndWait().get() == ButtonType.OK) {
                                        try {
                                            String jsonResponse = bookingData.delete(selectedBooking.getBookingId());
                                            DataResponse response = parseDataResponse(jsonResponse);
                                            if (response.isSuccess()) {
                                                loadInitialData();
                                                clearForm();
                                                showSuccess("Reserva eliminada con éxito");
                                            } else {
                                                showError("No se pudo eliminar la reserva: " + response.getMessage());
                                            }
                                        } catch (Exception e) {
                                            showError("Error al eliminar la reserva: " + e.getMessage());
                                        }
                                    }
                                }
                            }

                            @Deprecated
                            public void handleDeleteRoom(ActionEvent event) {
                                Room selectedRoom = reservedRoomsTableView.getSelectionModel().getSelectedItem();
                                if (selectedRoom != null && isEditing) {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                    alert.setTitle("Confirmar Eliminación");
                                    alert.setHeaderText(null);
                                    alert.setContentText("¿Está seguro que desea eliminar la habitación " +
                                            selectedRoom.getRoomNumber() + " de la reserva?");
                                    if (alert.showAndWait().get() == ButtonType.OK) {
                                        reservedRooms.remove(selectedRoom);
                                        updateCalculations();
                                        showSuccess("Habitación eliminada de la reserva");
                                    }
                                }
                            }

                            @FXML
                            public void handleEditReservation(ActionEvent event) {
                                Booking selectedBooking = (Booking) bookingListView.getSelectionModel().getSelectedItem();
                                if (selectedBooking != null) {
                                    loadBookingDetails(selectedBooking);
                                    setFieldsEnabled(true);
                                    isEditing = true;
                                    availableRoomsComboBox.setDisable(true);
                                    showSuccess("Editando reserva #" + selectedBooking.getBookingId());
                                    updateCalculations();
                                }
                            }

                            @FXML
                            public void handleUpdateBooking(ActionEvent actionEvent) {
                                if (!isEditing) {
                                    showError("No hay una reserva en modo edición.");
                                    return;
                                }
                                if (!validateBooking()) return;
                                try {
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
                                    String result = bookingData.update(booking);
                                    DataResponse response = parseDataResponse(result);
                                    if (response.isSuccess()) {
                                        showSuccess("Reserva actualizada exitosamente");
                                        loadInitialData();
                                        // Selecciona la reserva editada en la lista
                                        bookings.stream()
                                                .filter(b -> b.getBookingId() == bookingId)
                                                .findFirst()
                                                .ifPresent(b -> bookingListView.getSelectionModel().select(b));
                                        loadBookingDetails(booking);
                                        setFieldsEnabled(false);
                                        isEditing = false;
                                    } else {
                                        showError("Error al actualizar: " + response.getMessage());
                                    }
                                } catch (Exception e) {
                                    showError("Error al actualizar la reserva: " + e.getMessage());
                                }
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

                            private void setFieldsEnabled(boolean enabled) {
                                startDatePicker.setDisable(!enabled);
                                leavingDatePicker.setDisable(!enabled);
                                hotelComboBox.setDisable(!enabled);
                                guestComboBox.setDisable(!enabled);
                                clerkComboBox.setDisable(!enabled);
                                availableRoomsComboBox.setDisable(true); // Siempre desactivado hasta consultar disponibilidad
                                addRoomButton.setDisable(!enabled);
                                checkAvailabilityButton.setDisable(!enabled);
                                saveButton.setDisable(!enabled);
                                Room selectedRoom = reservedRoomsTableView.getSelectionModel().getSelectedItem();
                                deleteButton.setDisable(!enabled || selectedRoom == null);
                                if (enabled) {
                                    addRoomButton.setDisable(availableRoomsComboBox.getValue() == null);
                                    saveButton.setDisable(hotelComboBox.getValue() == null ||
                                            guestComboBox.getValue() == null ||
                                            clerkComboBox.getValue() == null);
                                }
                            }
                        }