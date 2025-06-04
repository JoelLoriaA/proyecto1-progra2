package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.shared.data.GuestData;
import com.MagicalStay.shared.domain.Guest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuestController {

    @FXML
    private TextField dniField;
    @FXML
    private ComboBox<String> nationalityComboBox;
    @FXML
    private Button searchButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private Label searchResultLabel;
    @FXML
    private TextField searchDniField;
    @FXML
    private Label validationLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField lastNameField;
    @FXML
    private Button searchByNameButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Button clearButton;
    @FXML
    private TextField searchNameField;
    @FXML
    private Button closeButton;
    @FXML
    private Button searchByEmailButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button saveButton;
    @FXML
    private TextField addressField;
    @FXML
    private TextField searchEmailField;

    // Tabla para mostrar resultados de búsqueda
    @FXML
    private TableView<Guest> guestTableView;
    @FXML
    private TableColumn<Guest, String> tableNameColumn;
    @FXML
    private TableColumn<Guest, String> tableLastNameColumn;
    @FXML
    private TableColumn<Guest, Integer> tableDniColumn;
    @FXML
    private TableColumn<Guest, String> tableEmailColumn;
    @FXML
    private TableColumn<Guest, Integer> tablePhoneColumn;
    @FXML
    private TableColumn<Guest, String> tableNationalityColumn;

    // Data access and utilities
    private GuestData guestData;
    private ObjectMapper objectMapper;
    private Guest selectedGuest;
    private boolean editMode = false;

    @FXML
    public void initialize() {
        try {
            // Initialize data access objects
            guestData = DataFactory.getGuestData();
            objectMapper = new ObjectMapper();

            // Setup table columns
            tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            tableLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tableDniColumn.setCellValueFactory(new PropertyValueFactory<>("dni"));
            tableEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            tablePhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
            tableNationalityColumn.setCellValueFactory(new PropertyValueFactory<>("nationality"));

            // Setup nationality combo box
            nationalityComboBox.setItems(FXCollections.observableArrayList(
                    "Costa Rica", "Estados Unidos", "México", "Guatemala", "Nicaragua",
                    "Honduras", "El Salvador", "Panamá", "Colombia", "Venezuela",
                    "España", "Francia", "Alemania", "Italia", "Reino Unido", "Canadá"
            ));

            // Initially disable edit buttons
            updateButton.setDisable(true);
            deleteButton.setDisable(true);

            // Load all guests initially
            loadAllGuests();

            statusLabel.setText("Sistema listo");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Inicialización",
                    "No se pudieron cargar los datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSearch(ActionEvent actionEvent) {
        String dniText = searchDniField.getText().trim();

        if (dniText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor ingrese un DNI para buscar");
            return;
        }

        try {
            int dni = Integer.parseInt(dniText);
            String jsonResponse = guestData.read(dni);
            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                Guest guest = objectMapper.convertValue(response.getData(), Guest.class);
                ObservableList<Guest> guestList = FXCollections.observableArrayList();
                guestList.add(guest);
                guestTableView.setItems(guestList);
                searchResultLabel.setText("Huésped encontrado: " + guest.getName() + " " + guest.getLastName());
                statusLabel.setText("Búsqueda por DNI completada");
            } else {
                guestTableView.setItems(FXCollections.observableArrayList());
                searchResultLabel.setText("No se encontró ningún huésped con DNI: " + dni);
                statusLabel.setText("Huésped no encontrado");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "El DNI debe ser un número válido");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error en la búsqueda: " + e.getMessage());
            statusLabel.setText("Error en la búsqueda");
        }
    }

    @FXML
    public void handleSearchByName(ActionEvent actionEvent) {
        String name = searchNameField.getText().trim().toLowerCase();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor ingrese un nombre para buscar");
            return;
        }

        try {
            String jsonResponse = guestData.readAll();
            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                List<Guest> allGuests = objectMapper.convertValue(response.getData(),
                        new TypeReference<List<Guest>>() {});

                List<Guest> filteredGuests = allGuests.stream()
                        .filter(guest -> guest.getName().toLowerCase().contains(name) ||
                                guest.getLastName().toLowerCase().contains(name))
                        .collect(Collectors.toList());

                ObservableList<Guest> guestList = FXCollections.observableArrayList(filteredGuests);
                guestTableView.setItems(guestList);

                if (filteredGuests.isEmpty()) {
                    searchResultLabel.setText("No se encontraron huéspedes con el nombre: " + searchNameField.getText());
                } else {
                    searchResultLabel.setText("Se encontraron " + filteredGuests.size() + " huésped(es)");
                }
                statusLabel.setText("Búsqueda por nombre completada");
            } else {
                guestTableView.setItems(FXCollections.observableArrayList());
                searchResultLabel.setText("Error al buscar huéspedes");
                statusLabel.setText("Error en la búsqueda");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error en la búsqueda: " + e.getMessage());
            statusLabel.setText("Error en la búsqueda");
        }
    }

    @FXML
    public void handleSearchByEmail(ActionEvent actionEvent) {
        String email = searchEmailField.getText().trim().toLowerCase();

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor ingrese un email para buscar");
            return;
        }

        try {
            String jsonResponse = guestData.readAll();
            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                List<Guest> allGuests = objectMapper.convertValue(response.getData(),
                        new TypeReference<List<Guest>>() {});

                List<Guest> filteredGuests = allGuests.stream()
                        .filter(guest -> guest.getEmail().toLowerCase().contains(email))
                        .collect(Collectors.toList());

                ObservableList<Guest> guestList = FXCollections.observableArrayList(filteredGuests);
                guestTableView.setItems(guestList);

                if (filteredGuests.isEmpty()) {
                    searchResultLabel.setText("No se encontraron huéspedes con el email: " + searchEmailField.getText());
                } else {
                    searchResultLabel.setText("Se encontraron " + filteredGuests.size() + " huésped(es)");
                }
                statusLabel.setText("Búsqueda por email completada");
            } else {
                guestTableView.setItems(FXCollections.observableArrayList());
                searchResultLabel.setText("Error al buscar huéspedes");
                statusLabel.setText("Error en la búsqueda");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error en la búsqueda: " + e.getMessage());
            statusLabel.setText("Error en la búsqueda");
        }
    }

    @FXML
    public void handleGuestSelection(MouseEvent event) {
        selectedGuest = guestTableView.getSelectionModel().getSelectedItem();
        if (selectedGuest != null) {
            // Fill form fields with selected guest data
            nameField.setText(selectedGuest.getName());
            lastNameField.setText(selectedGuest.getLastName());
            dniField.setText(String.valueOf(selectedGuest.getId()));
            phoneNumberField.setText(String.valueOf(selectedGuest.getPhoneNumber()));
            emailField.setText(selectedGuest.getEmail());
            addressField.setText(selectedGuest.getAddress());
            nationalityComboBox.setValue(selectedGuest.getNationality());

            // Enable edit and delete buttons
            updateButton.setDisable(false);
            deleteButton.setDisable(false);

            statusLabel.setText("Huésped seleccionado: " + selectedGuest.getName() + " " + selectedGuest.getLastName());
        }
    }

    @FXML
    public void handleSave(ActionEvent actionEvent) {
        if (validateFields()) {
            try {
                Guest guest = new Guest(
                        nameField.getText().trim(),
                        lastNameField.getText().trim(),
                        Integer.parseInt(dniField.getText().trim()),
                        Integer.parseInt(phoneNumberField.getText().trim()),
                        emailField.getText().trim(),
                        addressField.getText().trim(),
                        nationalityComboBox.getValue()
                );

                String jsonResponse = guestData.create(guest);
                DataResponse response = parseDataResponse(jsonResponse);

                if (response.isSuccess()) {
                    loadAllGuests();
                    clearFormFields();
                    statusLabel.setText("Huésped guardado exitosamente");
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Huésped registrado correctamente");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "No se pudo guardar el huésped: " + response.getMessage());
                    statusLabel.setText("Error al guardar");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "DNI y teléfono deben ser números válidos");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar: " + e.getMessage());
                statusLabel.setText("Error al guardar");
            }
        }
    }

    @FXML
    public void handleUpdate(ActionEvent actionEvent) {
        if (selectedGuest != null && validateFields()) {
            try {
                Guest updatedGuest = new Guest(
                        nameField.getText().trim(),
                        lastNameField.getText().trim(),
                        Integer.parseInt(dniField.getText().trim()),
                        Integer.parseInt(phoneNumberField.getText().trim()),
                        emailField.getText().trim(),
                        addressField.getText().trim(),
                        nationalityComboBox.getValue()
                );

                String jsonResponse = guestData.update(updatedGuest);
                DataResponse response = parseDataResponse(jsonResponse);

                if (response.isSuccess()) {
                    loadAllGuests();
                    clearFormFields();
                    updateButton.setDisable(true);
                    deleteButton.setDisable(true);
                    selectedGuest = null;
                    statusLabel.setText("Huésped actualizado exitosamente");
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Huésped actualizado correctamente");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el huésped: " + response.getMessage());
                    statusLabel.setText("Error al actualizar");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "DNI y teléfono deben ser números válidos");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al actualizar: " + e.getMessage());
                statusLabel.setText("Error al actualizar");
            }
        }
    }

    @FXML
    public void handleDelete(ActionEvent actionEvent) {
        if (selectedGuest != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar Eliminación");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("¿Está seguro que desea eliminar al huésped \"" +
                    selectedGuest.getName() + " " + selectedGuest.getLastName() + "\"?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String jsonResponse = guestData.delete(selectedGuest.getId());
                    DataResponse response = parseDataResponse(jsonResponse);

                    if (response.isSuccess()) {
                        loadAllGuests();
                        clearFormFields();
                        updateButton.setDisable(true);
                        deleteButton.setDisable(true);
                        selectedGuest = null;
                        statusLabel.setText("Huésped eliminado exitosamente");
                        showAlert(Alert.AlertType.INFORMATION, "Éxito", "Huésped eliminado correctamente");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el huésped: " + response.getMessage());
                        statusLabel.setText("Error al eliminar");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Error al eliminar: " + e.getMessage());
                    statusLabel.setText("Error al eliminar");
                }
            }
        }
    }

    @FXML
    public void handleClear(ActionEvent actionEvent) {
        clearFormFields();
        clearSearchFields();
        loadAllGuests();
        selectedGuest = null;
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        statusLabel.setText("Formulario limpiado");
    }

    @FXML
    public void handleClose(ActionEvent actionEvent) {
        try {
            DataFactory.closeAll();
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al cerrar: " + e.getMessage());
        }
    }

    private void loadAllGuests() {
        try {
            String jsonResponse = guestData.readAll();
            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                List<Guest> guests = objectMapper.convertValue(response.getData(),
                        new TypeReference<List<Guest>>() {});
                ObservableList<Guest> guestList = FXCollections.observableArrayList(guests);
                guestTableView.setItems(guestList);
                searchResultLabel.setText("Total de huéspedes: " + guests.size());
            } else {
                guestTableView.setItems(FXCollections.observableArrayList());
                searchResultLabel.setText("No se encontraron huéspedes");
            }
        } catch (Exception e) {
            guestTableView.setItems(FXCollections.observableArrayList());
            searchResultLabel.setText("Error al cargar huéspedes");
            statusLabel.setText("Error al cargar datos");
        }
    }

    private boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errorMessage.append("El nombre no puede estar vacío.\n");
        }

        if (lastNameField.getText().trim().isEmpty()) {
            errorMessage.append("El apellido no puede estar vacío.\n");
        }

        if (dniField.getText().trim().isEmpty()) {
            errorMessage.append("El DNI no puede estar vacío.\n");
        } else {
            try {
                Integer.parseInt(dniField.getText().trim());
            } catch (NumberFormatException e) {
                errorMessage.append("El DNI debe ser un número válido.\n");
            }
        }

        if (phoneNumberField.getText().trim().isEmpty()) {
            errorMessage.append("El teléfono no puede estar vacío.\n");
        } else {
            try {
                Integer.parseInt(phoneNumberField.getText().trim());
            } catch (NumberFormatException e) {
                errorMessage.append("El teléfono debe ser un número válido.\n");
            }
        }

        if (emailField.getText().trim().isEmpty()) {
            errorMessage.append("El email no puede estar vacío.\n");
        } else if (!emailField.getText().contains("@")) {
            errorMessage.append("El email debe tener un formato válido.\n");
        }

        if (addressField.getText().trim().isEmpty()) {
            errorMessage.append("La dirección no puede estar vacía.\n");
        }

        if (nationalityComboBox.getValue() == null) {
            errorMessage.append("Debe seleccionar una nacionalidad.\n");
        }

        if (errorMessage.length() > 0) {
            validationLabel.setText("Errores de validación encontrados");
            showAlert(Alert.AlertType.ERROR, "Errores de Validación", errorMessage.toString());
            return false;
        }

        validationLabel.setText("Todos los campos son válidos");
        return true;
    }

    private void clearFormFields() {
        nameField.clear();
        lastNameField.clear();
        dniField.clear();
        phoneNumberField.clear();
        emailField.clear();
        addressField.clear();
        nationalityComboBox.setValue(null);
        validationLabel.setText("Complete todos los campos requeridos");
    }

    private void clearSearchFields() {
        searchDniField.clear();
        searchNameField.clear();
        searchEmailField.clear();
        searchResultLabel.setText("");
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

    // Inner class for JSON response parsing
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