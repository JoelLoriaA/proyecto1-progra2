package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.shared.data.GuestData;
import com.MagicalStay.shared.domain.Guest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class GuestManagementController {

    // FXML elements for guest information
    @FXML
    private TextField nameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField dniField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> nationalityComboBox;

    @FXML
    private TextField addressField;

    // FXML elements for search
    @FXML
    private TextField searchDniField;

    @FXML
    private TextField searchNameField;

    @FXML
    private TextField searchEmailField;

    @FXML
    private Button searchButton;

    @FXML
    private Button searchByNameButton;

    @FXML
    private Button searchByEmailButton;

    @FXML
    private Label searchResultLabel;

    // FXML elements for table
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

    // FXML elements for actions
    @FXML
    private Button saveButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button closeButton;

    // FXML elements for validation and status
    @FXML
    private Label validationLabel;

    @FXML
    private Label statusLabel;

    // Data
    private ObservableList<Guest> guestList;
    private Guest selectedGuest;
    private boolean editMode = false;

    // Data access objects
    private GuestData guestData;
    private ObjectMapper objectMapper;

    @FXML
    private void initialize() {
        try {
            // Initialize data access objects
            guestData = DataFactory.getGuestData();
            objectMapper = new ObjectMapper();

            // Setup table columns
            tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            tableLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tableDniColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            tableEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            tablePhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
            tableNationalityColumn.setCellValueFactory(new PropertyValueFactory<>("nationality"));

            // Initialize nationality ComboBox
            setupNationalityComboBox();

            // Load all guests initially
            loadAllGuests();

            // Set initial button states
            updateButton.setDisable(true);
            deleteButton.setDisable(true);

            statusLabel.setText("Sistema listo");
            validationLabel.setText("Complete todos los campos requeridos");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Inicialización",
                    "No se pudieron cargar los datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNationalityComboBox() {
        nationalityComboBox.setItems(FXCollections.observableArrayList(
                "Costa Rica", "Estados Unidos", "México", "Guatemala", "El Salvador",
                "Honduras", "Nicaragua", "Panamá", "Colombia", "Venezuela",
                "Argentina", "Brasil", "Chile", "Perú", "Ecuador", "Uruguay",
                "Paraguay", "Bolivia", "España", "Francia", "Italia", "Alemania",
                "Reino Unido", "Canadá", "Australia", "Japón", "China", "India",
                "Rusia", "Otros"
        ));
    }

    private void loadAllGuests() {
        try {
            String jsonResponse = guestData.retrieveAll();
            System.out.println("JSON Response loadAllGuests: " + jsonResponse); // Debug

            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                try {
                    List<Guest> guests = objectMapper.convertValue(response.getData(),
                            new TypeReference<List<Guest>>() {});
                    guestList = FXCollections.observableArrayList(guests);
                    guestTableView.setItems(guestList);
                    statusLabel.setText("Se cargaron " + guests.size() + " huéspedes");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error deserializando lista completa: " + e.getMessage());
                    List<Guest> guests = deserializeGuestListManually(response.getData());
                    if (guests != null) {
                        guestList = FXCollections.observableArrayList(guests);
                        guestTableView.setItems(guestList);
                        statusLabel.setText("Se cargaron " + guests.size() + " huéspedes");
                    } else {
                        guestList = FXCollections.observableArrayList();
                        guestTableView.setItems(guestList);
                        statusLabel.setText("Error deserializando huéspedes");
                    }
                }
            } else {
                guestList = FXCollections.observableArrayList();
                guestTableView.setItems(guestList);
                statusLabel.setText("No se encontraron huéspedes: " + response.getMessage());
            }
        } catch (Exception e) {
            guestList = FXCollections.observableArrayList();
            guestTableView.setItems(guestList);
            statusLabel.setText("Error al cargar huéspedes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String dniText = searchDniField.getText().trim();

        if (dniText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor ingrese un DNI para buscar");
            return;
        }

        try {
            int dni = Integer.parseInt(dniText);
            System.out.println("Buscando DNI: " + dni); // Debug

            // Primero intentamos usar el método retrieveById
            String jsonResponse = guestData.retrieveById(dni);
            System.out.println("JSON Response retrieveById: " + jsonResponse); // Debug

            Guest foundGuest = null;

            // Si el retrieveById no funciona, intentamos con findById
            if (jsonResponse == null || jsonResponse.trim().isEmpty() || jsonResponse.equals("null")) {
                System.out.println("retrieveById falló, intentando con findById"); // Debug
                foundGuest = guestData.findById(dni);

                if (foundGuest != null) {
                    // Crear respuesta exitosa manualmente
                    ObservableList<Guest> searchResults = FXCollections.observableArrayList();
                    searchResults.add(foundGuest);
                    guestTableView.setItems(searchResults);
                    searchResultLabel.setText("Se encontró el huésped con DNI: " + dni);
                    statusLabel.setText("Búsqueda completada (findById)");
                    return;
                }
            } else {
                // Procesar respuesta normal de retrieveById
                DataResponse response = parseDataResponse(jsonResponse);

                if (response.isSuccess() && response.getData() != null) {
                    foundGuest = objectMapper.convertValue(response.getData(), Guest.class);

                    if (foundGuest != null) {
                        ObservableList<Guest> searchResults = FXCollections.observableArrayList();
                        searchResults.add(foundGuest);
                        guestTableView.setItems(searchResults);
                        searchResultLabel.setText("Se encontró el huésped con DNI: " + dni);
                        statusLabel.setText("Búsqueda completada (retrieveById)");
                        return;
                    }
                }
            }

            // Si llegamos aquí, no se encontró el huésped
            guestTableView.setItems(FXCollections.observableArrayList());
            searchResultLabel.setText("No se encontró huésped con DNI: " + dni);
            statusLabel.setText("No se encontraron resultados");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "El DNI debe ser un número válido");
        } catch (Exception e) {
            System.out.println("Error en búsqueda: " + e.getMessage()); // Debug
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error en la búsqueda: " + e.getMessage());
            statusLabel.setText("Error en la búsqueda");
        }
    }

    @FXML
    private void handleSearchByName(ActionEvent event) {
        String name = searchNameField.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor ingrese un nombre para buscar");
            return;
        }

        try {
            System.out.println("Buscando por nombre: " + name); // Debug
            String jsonResponse = guestData.retrieveByName(name);
            System.out.println("JSON Response retrieveByName: " + jsonResponse); // Debug

            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess() && response.getData() != null) {
                try {
                    List<Guest> guests = objectMapper.convertValue(response.getData(),
                            new TypeReference<List<Guest>>() {});

                    if (guests != null && !guests.isEmpty()) {
                        ObservableList<Guest> searchResults = FXCollections.observableArrayList(guests);
                        guestTableView.setItems(searchResults);
                        searchResultLabel.setText("Se encontraron " + guests.size() + " huésped(es) con nombre: " + name);
                        statusLabel.setText("Búsqueda por nombre completada");
                    } else {
                        guestTableView.setItems(FXCollections.observableArrayList());
                        searchResultLabel.setText("No se encontraron huéspedes con nombre: " + name);
                        statusLabel.setText("No se encontraron resultados");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Error deserializando lista por nombre: " + e.getMessage());
                    List<Guest> guests = deserializeGuestListManually(response.getData());
                    if (guests != null && !guests.isEmpty()) {
                        ObservableList<Guest> searchResults = FXCollections.observableArrayList(guests);
                        guestTableView.setItems(searchResults);
                        searchResultLabel.setText("Se encontraron " + guests.size() + " huésped(es) con nombre: " + name);
                        statusLabel.setText("Búsqueda por nombre completada");
                    } else {
                        guestTableView.setItems(FXCollections.observableArrayList());
                        searchResultLabel.setText("No se encontraron huéspedes con nombre: " + name);
                        statusLabel.setText("No se encontraron resultados");
                    }
                }
            } else {
                guestTableView.setItems(FXCollections.observableArrayList());
                searchResultLabel.setText("No se encontraron huéspedes con nombre: " + name);
                statusLabel.setText("No se encontraron resultados");
            }
        } catch (Exception e) {
            System.out.println("Error en búsqueda por nombre: " + e.getMessage()); // Debug
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error en la búsqueda: " + e.getMessage());
            statusLabel.setText("Error en la búsqueda");
        }
    }

    @FXML
    private void handleSearchByEmail(ActionEvent event) {
        String email = searchEmailField.getText().trim();

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor ingrese un email para buscar");
            return;
        }

        try {
            System.out.println("Buscando por email: " + email); // Debug
            String jsonResponse = guestData.retrieveByEmail(email);
            System.out.println("JSON Response retrieveByEmail: " + jsonResponse); // Debug

            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess() && response.getData() != null) {
                try {
                    List<Guest> guests = objectMapper.convertValue(response.getData(),
                            new TypeReference<List<Guest>>() {});

                    if (guests != null && !guests.isEmpty()) {
                        ObservableList<Guest> searchResults = FXCollections.observableArrayList(guests);
                        guestTableView.setItems(searchResults);
                        searchResultLabel.setText("Se encontraron " + guests.size() + " huésped(es) con email: " + email);
                        statusLabel.setText("Búsqueda por email completada");
                    } else {
                        guestTableView.setItems(FXCollections.observableArrayList());
                        searchResultLabel.setText("No se encontraron huéspedes con email: " + email);
                        statusLabel.setText("No se encontraron resultados");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Error deserializando lista por email: " + e.getMessage());
                    List<Guest> guests = deserializeGuestListManually(response.getData());
                    if (guests != null && !guests.isEmpty()) {
                        ObservableList<Guest> searchResults = FXCollections.observableArrayList(guests);
                        guestTableView.setItems(searchResults);
                        searchResultLabel.setText("Se encontraron " + guests.size() + " huésped(es) con email: " + email);
                        statusLabel.setText("Búsqueda por email completada");
                    } else {
                        guestTableView.setItems(FXCollections.observableArrayList());
                        searchResultLabel.setText("No se encontraron huéspedes con email: " + email);
                        statusLabel.setText("No se encontraron resultados");
                    }
                }
            } else {
                guestTableView.setItems(FXCollections.observableArrayList());
                searchResultLabel.setText("No se encontraron huéspedes con email: " + email);
                statusLabel.setText("No se encontraron resultados");
            }
        } catch (Exception e) {
            System.out.println("Error en búsqueda por email: " + e.getMessage()); // Debug
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error en la búsqueda: " + e.getMessage());
            statusLabel.setText("Error en la búsqueda");
        }
    }

    @FXML
    private void handleGuestSelection(MouseEvent event) {
        selectedGuest = guestTableView.getSelectionModel().getSelectedItem();
        if (selectedGuest != null) {
            // Fill fields with selected guest data
            nameField.setText(selectedGuest.getName());
            lastNameField.setText(selectedGuest.getLastName());
            dniField.setText(String.valueOf(selectedGuest.getId()));
            phoneNumberField.setText(String.valueOf(selectedGuest.getPhoneNumber()));
            emailField.setText(selectedGuest.getEmail());
            addressField.setText(selectedGuest.getAddress());
            nationalityComboBox.setValue(selectedGuest.getNationality());

            // Enable update and delete buttons
            updateButton.setDisable(false);
            deleteButton.setDisable(false);

            statusLabel.setText("Huésped seleccionado: " + selectedGuest.getName() + " " + selectedGuest.getLastName());
            validationLabel.setText("Huésped cargado correctamente");
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (validateFields()) {
            try {
                // Check if DNI already exists (for new guests)
                int dni = Integer.parseInt(dniField.getText().trim());

                // Verificar si existe usando ambos métodos
                Guest existingGuest = null;
                try {
                    existingGuest = guestData.findById(dni);
                } catch (Exception e) {
                    System.out.println("Error verificando existencia: " + e.getMessage());
                }

                if (existingGuest != null) {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Ya existe un huésped con el DNI: " + dni);
                    return;
                }

                Guest newGuest = createGuestFromFields();
                String jsonResponse = guestData.create(newGuest);
                System.out.println("JSON Response create: " + jsonResponse); // Debug

                DataResponse response = parseDataResponse(jsonResponse);

                if (response.isSuccess()) {
                    loadAllGuests();
                    clearFields();
                    statusLabel.setText("Huésped guardado exitosamente");
                    validationLabel.setText("Huésped creado correctamente");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "No se pudo guardar el huésped: " + response.getMessage());
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "DNI y teléfono deben ser números válidos");
            } catch (Exception e) {
                System.out.println("Error guardando huésped: " + e.getMessage()); // Debug
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Error al guardar el huésped: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedGuest != null && validateFields()) {
            try {
                Guest updatedGuest = createGuestFromFields();
                String jsonResponse = guestData.update(updatedGuest);
                System.out.println("JSON Response update: " + jsonResponse); // Debug

                DataResponse response = parseDataResponse(jsonResponse);

                if (response.isSuccess()) {
                    loadAllGuests();
                    clearFields();
                    selectedGuest = null;
                    updateButton.setDisable(true);
                    deleteButton.setDisable(true);
                    statusLabel.setText("Huésped actualizado exitosamente");
                    validationLabel.setText("Actualización completada");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "No se pudo actualizar el huésped: " + response.getMessage());
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "DNI y teléfono deben ser números válidos");
            } catch (Exception e) {
                System.out.println("Error actualizando huésped: " + e.getMessage()); // Debug
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Error al actualizar el huésped: " + e.getMessage());
            }
        } else if (selectedGuest == null) {
            showAlert(Alert.AlertType.WARNING, "Advertencia",
                    "Por favor seleccione un huésped de la tabla para actualizar");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedGuest != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText(null);
            alert.setContentText("¿Está seguro que desea eliminar al huésped \"" +
                    selectedGuest.getName() + " " + selectedGuest.getLastName() + "\"?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String jsonResponse = guestData.delete(selectedGuest.getId());
                    System.out.println("JSON Response delete: " + jsonResponse); // Debug

                    DataResponse response = parseDataResponse(jsonResponse);

                    if (response.isSuccess()) {
                        loadAllGuests();
                        clearFields();
                        selectedGuest = null;
                        updateButton.setDisable(true);
                        deleteButton.setDisable(true);
                        statusLabel.setText("Huésped eliminado exitosamente");
                        validationLabel.setText("Eliminación completada");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "No se pudo eliminar el huésped: " + response.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println("Error eliminando huésped: " + e.getMessage()); // Debug
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Error al eliminar el huésped: " + e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Advertencia",
                    "Por favor seleccione un huésped de la tabla para eliminar");
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearFields();
        selectedGuest = null;
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        // Clear search fields
        searchDniField.clear();
        searchNameField.clear();
        searchEmailField.clear();
        searchResultLabel.setText("");

        // Reload all guests
        loadAllGuests();

        statusLabel.setText("Campos limpiados");
        validationLabel.setText("Complete todos los campos requeridos");
    }

    @FXML
    private void handleClose(ActionEvent event) {
        try {
            if (guestData != null) {
                guestData.close();
            }

            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error al cerrar la aplicación: " + e.getMessage());
        }
    }

    private Guest createGuestFromFields() {
        String name = nameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        int dni = Integer.parseInt(dniField.getText().trim());
        int phoneNumber = Integer.parseInt(phoneNumberField.getText().trim());
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String nationality = nationalityComboBox.getValue();

        return new Guest(name, lastName, dni, phoneNumber, email, address, nationality);
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
        } else if (!isValidEmail(emailField.getText().trim())) {
            errorMessage.append("El formato del email no es válido.\n");
        }

        if (addressField.getText().trim().isEmpty()) {
            errorMessage.append("La dirección no puede estar vacía.\n");
        }

        if (nationalityComboBox.getValue() == null || nationalityComboBox.getValue().isEmpty()) {
            errorMessage.append("Debe seleccionar una nacionalidad.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", errorMessage.toString());
            validationLabel.setText("Corrija los errores en los campos");
            return false;
        }

        validationLabel.setText("Todos los campos son válidos");
        return true;
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") &&
                email.indexOf("@") < email.lastIndexOf(".");
    }

    private void clearFields() {
        nameField.clear();
        lastNameField.clear();
        dniField.clear();
        phoneNumberField.clear();
        emailField.clear();
        addressField.clear();
        nationalityComboBox.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private DataResponse parseDataResponse(String jsonResponse) throws Exception {
        if (jsonResponse == null || jsonResponse.trim().isEmpty() || jsonResponse.equals("null")) {
            // Crear una respuesta de error si no hay datos
            DataResponse response = new DataResponse();
            response.setSuccess(false);
            response.setMessage("No se recibió respuesta del servidor");
            response.setData(null);
            return response;
        }
        return objectMapper.readValue(jsonResponse, DataResponse.class);
    }

    // Método auxiliar para deserialización manual de un Guest
    private Guest deserializeGuestManually(Object data) {
        try {
            if (data instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) data;

                String name = (String) map.get("name");
                String lastName = (String) map.get("lastName");
                Object idObj = map.get("id");
                Object phoneObj = map.get("phoneNumber");
                String email = (String) map.get("email");
                String address = (String) map.get("address");
                String nationality = (String) map.get("nationality");

                // Convertir números de manera segura
                int id = convertToInt(idObj);
                int phoneNumber = convertToInt(phoneObj);

                return new Guest(name, lastName, id, phoneNumber, email, address, nationality);
            }
        } catch (Exception e) {
            System.out.println("Error en deserialización manual: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Método auxiliar para deserialización manual de lista de Guests
    @SuppressWarnings("unchecked")
    private java.util.List<Guest> deserializeGuestListManually(Object data) {
        java.util.List<Guest> guests = new java.util.ArrayList<>();
        try {
            if (data instanceof java.util.List) {
                java.util.List<Object> list = (java.util.List<Object>) data;
                for (Object item : list) {
                    Guest guest = deserializeGuestManually(item);
                    if (guest != null) {
                        guests.add(guest);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error en deserialización manual de lista: " + e.getMessage());
            e.printStackTrace();
        }
        return guests;
    }

    // Método auxiliar para convertir objetos a int de manera segura
    private int convertToInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        if (obj instanceof Number) return ((Number) obj).intValue();
        return 0;
    }

    // Inner class for JSON response parsing
    private static class DataResponse {
        private boolean success;
        private String message;
        private Object data;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Object getData() { return data; }
        public void setData(Object data) { this.data =  data;}
    }
}