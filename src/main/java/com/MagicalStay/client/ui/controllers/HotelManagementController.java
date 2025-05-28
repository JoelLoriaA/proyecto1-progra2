package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.shared.data.HotelData;
import com.MagicalStay.shared.domain.Hotel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class HotelManagementController {

    // Elementos FXML vinculados al archivo de la interfaz gráfica
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
    private TextField codeTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label statusLabel;

    // Datos y lógica
    private ObservableList<Hotel> hotelList;
    private Hotel selectedHotel;
    private boolean editMode = false; // Modo edición o creación
    private HotelData hotelData;
    private ObjectMapper objectMapper;
    @FXML
    private Button searchButton;
    @FXML
    private TableColumn guestIdColumn;
    @FXML
    private TableColumn roomTypeColumn;
    @FXML
    private Button manageGuestsButton;
    @FXML
    private TableColumn roomNumberColumn;
    @FXML
    private TableColumn guestEmailColumn;
    @FXML
    private Button manageRoomsButton;
    @FXML
    private TableColumn guestNameColumn;
    @FXML
    private TableColumn roomStatusColumn;
    @FXML
    private Button closeButton;
    @FXML
    private TableView guestsTableView;
    @FXML
    private TableColumn roomPriceColumn;
    @FXML
    private TableView roomsTableView;

    @FXML
    private void initialize() {
        hotelData = new HotelData();
        objectMapper = new ObjectMapper();

        // Inicializar la lista de hoteles y cargar los datos desde el servidor
        loadHotels();
        hotelListView.setItems(hotelList);

        // Agregar marcador cuando no hay hoteles disponibles
        hotelListView.setPlaceholder(new Label("No hay hoteles disponibles. Por favor, agrega uno nuevo."));

        // Configurar el ListView para mostrar los datos
        hotelListView.setCellFactory(lv -> new ListCell<Hotel>() {
            @Override
            protected void updateItem(Hotel hotel, boolean empty) {
                super.updateItem(hotel, empty);
                if (empty || hotel == null) {
                    setText(null);
                } else {
                    setText(hotel.getName() + " - " + hotel.getAddress());
                }
            }
        });

        // Deshabilitar campos hasta que se seleccionen o creen datos
        setFieldsEnabled(false);
    }

    /** Carga la lista de hoteles desde el servidor. */
    private void loadHotels() {
        try {
            String jsonResponse = hotelData.readAll(); // Llamada al servidor

            // Detectar si la respuesta está vacía
            if (jsonResponse == null || jsonResponse.isEmpty() || jsonResponse.equals("[]")) {
                hotelList = FXCollections.observableArrayList(); // Inicializar lista vacía
                statusLabel.setText("No hay hoteles disponibles. Presiona 'Agregar' para crear el primero.");
            } else {
                List<Hotel> hotels = objectMapper.readValue(jsonResponse, new TypeReference<>() {});
                hotelList = FXCollections.observableArrayList(hotels);
                statusLabel.setText("Hoteles cargados correctamente.");
            }
        } catch (IOException e) {
            hotelList = FXCollections.observableArrayList(); // Inicializar lista vacía en caso de error
            showError("Error al cargar hoteles", "No se pudo cargar la lista de hoteles: " + e.getMessage());
        }
    }

    /** Activa o desactiva los campos editables del formulario. */
    private void setFieldsEnabled(boolean enabled) {
        codeTextField.setDisable(true); // Siempre desactivado (es automático o inmutable)
        nameTextField.setDisable(!enabled);
        addressTextArea.setDisable(!enabled);
        saveButton.setDisable(!enabled);
        cancelButton.setDisable(!enabled);
    }

    /** Limpia los campos del formulario. */
    private void clearFields() {
        codeTextField.clear();
        nameTextField.clear();
        addressTextArea.clear();
    }

    /** Muestra mensajes de error. */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Maneja la acción de búsqueda de hoteles. */
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchTextField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            hotelListView.setItems(hotelList); // Restablecer la lista completa
        } else {
            try {
                String jsonResponse = hotelData.findByName(searchText);
                List<Hotel> filteredHotels = objectMapper.readValue(jsonResponse, new TypeReference<>() {});
                hotelListView.setItems(FXCollections.observableArrayList(filteredHotels));
            } catch (IOException e) {
                showError("Error en búsqueda", "Hubo un problema al buscar hoteles: " + e.getMessage());
            }
        }
    }

    /** Maneja la acción de agregar un nuevo hotel. */
    @FXML
    private void handleAddHotel(ActionEvent event) {
        clearFields();
        setFieldsEnabled(true);
        editMode = false;
        codeTextField.setText("[Automático]"); // Indicador visual para el usuario
        statusLabel.setText("Por favor, complete los campos para agregar un nuevo hotel.");
    }

    /** Maneja la acción de editar un hotel seleccionado. */
    @FXML
    private void handleEditHotel(ActionEvent event) {
        selectedHotel = hotelListView.getSelectionModel().getSelectedItem();
        if (selectedHotel != null) {
            editMode = true;
            setFieldsEnabled(true);

            // Rellenar los campos con los datos del hotel seleccionado
            codeTextField.setText(String.valueOf(selectedHotel.getHotelId()));
            nameTextField.setText(selectedHotel.getName());
            addressTextArea.setText(selectedHotel.getAddress());

            statusLabel.setText("Editando hotel: " + selectedHotel.getName());
        }
    }

    /**
     * Maneja la acción de guardar un hotel.
     * Aplica tanto para crear uno nuevo como editar uno existente.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        String name = nameTextField.getText().trim();
        String address = addressTextArea.getText().trim();

        // Validación de campos obligatorios
        if (name.isEmpty() || address.isEmpty()) {
            showError("Validación fallida", "El nombre y la dirección son obligatorios.");
            return;
        }

        try {
            if (editMode) {
                // Actualizar un hotel existente
                selectedHotel.setName(name);
                selectedHotel.setAddress(address);

                String jsonResponse = hotelData.update(selectedHotel);
                statusLabel.setText("Hotel actualizado con éxito.");
            } else {
                // Crear un nuevo hotel
                Hotel newHotel = new Hotel(0, name, address, null); // ID = 0 porque lo genera el servidor
                String jsonResponse = hotelData.create(newHotel);

                // Validar si es el primer hotel que se agrega
                if (hotelList.isEmpty()) {
                    statusLabel.setText("Primer hotel agregado con éxito.");
                } else {
                    statusLabel.setText("Hotel agregado con éxito.");
                }
            }

            // Recargar la lista de hoteles
            loadHotels();
            hotelListView.setItems(hotelList);

            // Limpiar y desactivar el formulario
            clearFields();
            setFieldsEnabled(false);
        } catch (IOException e) {
            showError("Error al guardar", "Hubo un problema al guardar los datos: " + e.getMessage());
        }
    }

    /** Maneja la acción de eliminar un hotel seleccionado. */
    @FXML
    private void handleDeleteHotel(ActionEvent event) {
        selectedHotel = hotelListView.getSelectionModel().getSelectedItem();
        if (selectedHotel != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText(null);
            alert.setContentText("¿Está seguro que desea eliminar el hotel seleccionado?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String jsonResponse = hotelData.delete(selectedHotel.getHotelId());
                    statusLabel.setText("Hotel eliminado con éxito.");

                    // Recargar la lista
                    loadHotels();
                    hotelListView.setItems(hotelList);

                    // Limpiar el formulario
                    clearFields();
                } catch (IOException e) {
                    showError("Error al eliminar", "Hubo un problema al eliminar el hotel: " + e.getMessage());
                }
            }
        }
    }

    /** Maneja la acción de cancelar cualquier operación. */
    @FXML
    private void handleCancel(ActionEvent event) {
        clearFields();
        setFieldsEnabled(false);
        statusLabel.setText("Operación cancelada.");
    }

    @FXML
    public void handleHotelSelection(Event event) {
    }

    @FXML
    public void handleManageRooms(ActionEvent actionEvent) {
    }

    @FXML
    public void handleClose(ActionEvent actionEvent) {
    }

    @FXML
    public void handleManageGuests(ActionEvent actionEvent) {
    }

}