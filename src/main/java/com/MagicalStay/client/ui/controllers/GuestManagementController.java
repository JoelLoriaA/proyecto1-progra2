package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.shared.data.GuestData;
import com.MagicalStay.shared.domain.Guest;
import com.MagicalStay.shared.domain.Hotel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class GuestManagementController {
    @FXML
    private TextField searchTextField;
    @FXML
    private ListView<Guest> guestListView;
    @FXML
    private TextField nameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField dniField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField nationalityField;
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

    private GuestData guestData;
    private ObjectMapper objectMapper;
    private ObservableList<Guest> guestList;
    private Guest selectedGuest;
    private boolean editMode = false;
    private Hotel selectedHotel;

    @FXML
    private void initialize() {
        try {
            guestData = DataFactory.getGuestData();
            objectMapper = new ObjectMapper();

            loadGuestsFromFile();

            guestListView.setItems(guestList);
            guestListView.setCellFactory(lv -> new ListCell<Guest>() {
                @Override
                protected void updateItem(Guest guest, boolean empty) {
                    super.updateItem(guest, empty);
                    if (empty || guest == null) {
                        setText(null);
                    } else {
                        setText(guest.getName() + " " + guest.getLastName() + " - DNI: " + guest.getDni());
                    }
                }
            });


            setFieldsEnabled(false);
            editButton.setDisable(true);
            deleteButton.setDisable(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Inicialización",
                    "No se pudieron cargar los datos: " + e.getMessage());
        }
    }

    private void loadGuestsFromFile() {
        try {
            String jsonResponse = guestData.readAll();
            JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

            if (response.isSuccess()) {
                List<Guest> guests = objectMapper.convertValue(response.getData(),
                        new TypeReference<List<Guest>>() {});
                guestList = FXCollections.observableArrayList(guests);
            } else {
                guestList = FXCollections.observableArrayList();
                statusLabel.setText("No se encontraron huéspedes: " + response.getMessage());
            }
        } catch (Exception e) {
            guestList = FXCollections.observableArrayList();
            statusLabel.setText("Error al cargar huéspedes: " + e.getMessage());
        }
    }

    @FXML
    private void handleGuestSelection(MouseEvent event) {
        selectedGuest = guestListView.getSelectionModel().getSelectedItem();
        if (selectedGuest != null) {
            nameField.setText(selectedGuest.getName());
            lastNameField.setText(selectedGuest.getLastName());
            dniField.setText(String.valueOf(selectedGuest.getDni()));
            phoneField.setText(String.valueOf(selectedGuest.getPhoneNumber()));
            emailField.setText(selectedGuest.getEmail());
            addressField.setText(selectedGuest.getAddress());
            nationalityField.setText(selectedGuest.getNationality());

            editButton.setDisable(false);
            deleteButton.setDisable(false);
        }
    }

    @FXML
    private void handleAddGuest() {
        clearFields();
        setFieldsEnabled(true);
        editMode = false;
        dniField.setDisable(false);
        saveButton.setDisable(false);
        cancelButton.setDisable(false);
        statusLabel.setText("Agregando nuevo huésped...");
    }

    @FXML
    private void handleEditGuest() {
        if (selectedGuest != null) {
            setFieldsEnabled(true);
            editMode = true;
            dniField.setDisable(true);
            saveButton.setDisable(false);
            cancelButton.setDisable(false);
            statusLabel.setText("Editando huésped: " + selectedGuest.getName());
        }
    }

    @FXML
    private void handleDeleteGuest() {
        if (selectedGuest != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText(null);
            alert.setContentText("¿Está seguro que desea eliminar al huésped \"" +
                    selectedGuest.getName() + " " + selectedGuest.getLastName() + "\"?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String jsonResponse = guestData.delete(selectedGuest.getDni());
                    JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

                    if (response.isSuccess()) {
                        loadGuestsFromFile();
                        clearFields();
                        setFieldsEnabled(false);
                        statusLabel.setText("Huésped eliminado con éxito");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "No se pudo eliminar el huésped: " + response.getMessage());
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Error al eliminar el huésped: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        if (validateFields()) {
            try {
                Guest guest = new Guest(
                        nameField.getText(),
                        lastNameField.getText(),
                        Integer.parseInt(dniField.getText()),
                        Integer.parseInt(phoneField.getText()),
                        emailField.getText(),
                        addressField.getText(),
                        nationalityField.getText()
                );

                String jsonResponse;
                if (editMode) {
                    jsonResponse = guestData.update(guest);
                } else {
                    jsonResponse = guestData.create(guest);
                }

                JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

                if (response.isSuccess()) {
                    loadGuestsFromFile();
                    setFieldsEnabled(false);
                    saveButton.setDisable(true);
                    cancelButton.setDisable(true);
                    statusLabel.setText("Huésped guardado con éxito");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "No se pudo guardar el huésped: " + response.getMessage());
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Error al guardar el huésped: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        if (editMode && selectedGuest != null) {
            handleGuestSelection(null);
        } else {
            clearFields();
        }
        setFieldsEnabled(false);
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        statusLabel.setText("Operación cancelada");
    }

    private void clearFields() {
        nameField.clear();
        lastNameField.clear();
        dniField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
        nationalityField.clear();
    }

    private void setFieldsEnabled(boolean enabled) {
        nameField.setDisable(!enabled);
        lastNameField.setDisable(!enabled);
        dniField.setDisable(!enabled);
        phoneField.setDisable(!enabled);
        emailField.setDisable(!enabled);
        addressField.setDisable(!enabled);
        nationalityField.setDisable(!enabled);
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
        }
        try {
            if (!dniField.getText().trim().isEmpty()) {
                Integer.parseInt(dniField.getText());
            }
        } catch (NumberFormatException e) {
            errorMessage.append("El DNI debe ser un número válido.\n");
        }
        try {
            if (!phoneField.getText().trim().isEmpty()) {
                Integer.parseInt(phoneField.getText());
            }
        } catch (NumberFormatException e) {
            errorMessage.append("El teléfono debe ser un número válido.\n");
        }

        // Agregar validación de email
        if (!emailField.getText().trim().isEmpty()) {
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!emailField.getText().matches(emailRegex)) {
                errorMessage.append("El formato del email no es válido.\n");
            }
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", errorMessage.toString());
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Clase auxiliar para manejar las respuestas JSON
    private static class JsonResponse {
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