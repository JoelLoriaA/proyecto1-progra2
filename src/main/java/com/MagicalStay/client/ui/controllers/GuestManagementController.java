package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.shared.data.GuestData;
import com.MagicalStay.shared.domain.Guest;
import com.MagicalStay.shared.domain.Hotel;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class GuestManagementController {

    @FXML
    private ComboBox<String> searchTypeComboBox;

    @FXML
    private TextField searchTextField;

    @FXML
    private Button searchButton;

    @FXML
    private Label searchResultLabel;

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
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;


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

    @FXML
    private Label validationLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button clearButton;


    @FXML
    private Label statusLabel;

    @FXML
    private Button closeButton;


    private ObservableList<Guest> guestList;
    private Guest selectedGuest;
    private boolean editMode = false;


    private GuestData guestData;
    private ObjectMapper objectMapper;

    @FXML
    private void initialize() {
        try {
            setFieldsEnabled(false);

            guestData = DataFactory.getGuestData();
            objectMapper = new ObjectMapper();


            tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            tableLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tableDniColumn.setCellValueFactory(new PropertyValueFactory<>("dni"));
            tableEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

            searchTypeComboBox.setItems(FXCollections.observableArrayList(
                    "Por Nombre",
                    "Por Email",
                    "Por DNI",
                    "Todos"
            ));
            searchTypeComboBox.setValue("Por Nombre");


            setupNationalityComboBox();


            loadGuestsFromFile();


            guestTableView.setItems(guestList);


            editButton.setDisable(true);
            deleteButton.setDisable(true);
            updateButton.setDisable(true);



            statusLabel.setText("Listo");
            validationLabel.setText("Complete todos los campos requeridos");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Inicialización",
                    "No se pudieron cargar los datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNationalityComboBox() {
        ObservableList<String> countries = FXCollections.observableArrayList();


        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            if (!country.isEmpty() && !countries.contains(country)) {
                countries.add(country);
            }
        }


        FXCollections.sort(countries);


        ObservableList<String> commonCountries = FXCollections.observableArrayList(
                "Costa Rica", "Estados Unidos", "México", "España", "Colombia", "Argentina"
        );


        countries.removeAll(commonCountries);


        ObservableList<String> finalList = FXCollections.observableArrayList();
        finalList.addAll(commonCountries);
        finalList.add("---");
        finalList.addAll(countries);

        nationalityComboBox.setItems(finalList);
        nationalityComboBox.setValue("Costa Rica");
    }

    private void loadGuestsFromFile() {
        try {
            String jsonResponse = guestData.retrieveAll();
            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                List<Guest> guests = objectMapper.convertValue(response.getData(),
                        new TypeReference<List<Guest>>() {});
                guestList = FXCollections.observableArrayList(guests);
                searchResultLabel.setText("Se encontraron " + guests.size() + " huéspedes");
            } else {
                guestList = FXCollections.observableArrayList();
                searchResultLabel.setText("No se encontraron huéspedes: " + response.getMessage());
            }
        } catch (Exception e) {
            guestList = FXCollections.observableArrayList();
            searchResultLabel.setText("Error al cargar huéspedes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGuestSelection(MouseEvent event) {
        selectedGuest = guestTableView.getSelectionModel().getSelectedItem();
        if (selectedGuest != null) {

            nameField.setText(selectedGuest.getName());
            lastNameField.setText(selectedGuest.getLastName());
            dniField.setText(String.valueOf(selectedGuest.getDni()));
            phoneNumberField.setText(String.valueOf(selectedGuest.getPhoneNumber()));
            emailField.setText(selectedGuest.getEmail());
            addressField.setText(selectedGuest.getAddress());
            nationalityComboBox.setValue(selectedGuest.getNationality());


            editButton.setDisable(false);
            deleteButton.setDisable(false);


            setFieldsEnabled(false);

            statusLabel.setText("Huésped seleccionado: " + selectedGuest.getName() + " " + selectedGuest.getLastName());
            validationLabel.setText("Huésped cargado correctamente");
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchTextField.getText().trim();
        String searchType = searchTypeComboBox.getValue();

        try {
            String jsonResponse;

            if (searchText.isEmpty() || searchType.equals("Todos")) {
                jsonResponse = guestData.retrieveAll();
            } else {
                switch (searchType) {
                    case "Por Nombre":
                        jsonResponse = guestData.retrieveByName(searchText);
                        break;
                    case "Por Email":
                        jsonResponse = guestData.retrieveByEmail(searchText);
                        break;
                    case "Por DNI":
                        try {
                            int dni = Integer.parseInt(searchText);
                            jsonResponse = guestData.retrieveById(dni);
                        } catch (NumberFormatException e) {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "El DNI debe ser un número válido");
                            return;
                        }
                        break;
                    default:
                        jsonResponse = guestData.retrieveAll();
                }
            }

            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                List<Guest> guests;
                if (response.getData() instanceof List) {
                    guests = objectMapper.convertValue(response.getData(),
                            new TypeReference<List<Guest>>() {});
                } else {

                    Guest guest = objectMapper.convertValue(response.getData(), Guest.class);
                    guests = Collections.singletonList(guest);
                }
                guestTableView.setItems(FXCollections.observableArrayList(guests));
                searchResultLabel.setText("Se encontraron " + guests.size() + " resultado(s)");
                statusLabel.setText("Búsqueda completada con éxito");
            } else {
                guestTableView.setItems(FXCollections.observableArrayList());
                searchResultLabel.setText("No se encontraron resultados");
                statusLabel.setText("No se encontraron resultados: " + response.getMessage());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error en la búsqueda: " + e.getMessage());
            statusLabel.setText("Error en la búsqueda: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        clearFields();
        setFieldsEnabled(true);
        editMode = false;

        guestTableView.getSelectionModel().clearSelection();
        selectedGuest = null;


        saveButton.setDisable(false);
        updateButton.setDisable(true);

        editButton.setDisable(true);
        deleteButton.setDisable(true);

        statusLabel.setText("Agregando nuevo huésped...");
        validationLabel.setText("Complete todos los campos requeridos");
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (selectedGuest != null) {
            setFieldsEnabled(true);
            editMode = true;


            saveButton.setDisable(true);
            updateButton.setDisable(false);


            statusLabel.setText("Editando huésped: " + selectedGuest.getName() + " " + selectedGuest.getLastName());
            validationLabel.setText("Modifique los campos necesarios");
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
                    String jsonResponse = guestData.delete(selectedGuest.getDni());
                    DataResponse response = parseDataResponse(jsonResponse);

                    if (response.isSuccess()) {

                        loadGuestsFromFile();
                        guestTableView.setItems(guestList);
                        clearFields();


                        selectedGuest = null;


                        editButton.setDisable(true);
                        deleteButton.setDisable(true);
                        updateButton.setDisable(true);

                        statusLabel.setText("Huésped eliminado con éxito");
                        validationLabel.setText("Complete todos los campos requeridos");
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
    private void handleSave(ActionEvent event) {
        if (validateFields()) {
            try {
                int dni = Integer.parseInt(dniField.getText().trim());
                int phoneNumber = Integer.parseInt(phoneNumberField.getText().trim());

                Guest guest = new Guest(
                        nameField.getText().trim(),
                        lastNameField.getText().trim(),
                        dni,
                        phoneNumber,
                        emailField.getText().trim(),
                        addressField.getText().trim(),
                        nationalityComboBox.getValue()
                );

                String jsonResponse = guestData.create(guest);
                DataResponse response = parseDataResponse(jsonResponse);

                if (response.isSuccess()) {
                    loadGuestsFromFile();
                    guestTableView.setItems(guestList);

                    setFieldsEnabled(false);
                    saveButton.setDisable(true);


                    for (Guest g : guestList) {
                        if (g.getDni() == guest.getDni()) {
                            guestTableView.getSelectionModel().select(g);
                            break;
                        }
                    }

                    statusLabel.setText("Huésped guardado con éxito");
                    validationLabel.setText("Huésped creado correctamente");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "No se pudo guardar el huésped: " + response.getMessage());
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error de Validación",
                        "DNI y Teléfono deben ser números válidos");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Error al guardar el huésped: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedGuest != null && validateFields()) {
            try {
                int newDni = Integer.parseInt(dniField.getText().trim());
                int phoneNumber = Integer.parseInt(phoneNumberField.getText().trim());


                if (newDni != selectedGuest.getDni()) {

                    String checkResponse = guestData.retrieveById(newDni);
                    DataResponse checkDataResponse = parseDataResponse(checkResponse);
                    if (checkDataResponse.isSuccess()) {
                        showAlert(Alert.AlertType.ERROR, "Error de Validación",
                                "Ya existe un huésped con el DNI " + newDni);
                        return;
                    }
                }


                if (newDni != selectedGuest.getDni()) {

                    String deleteResponse = guestData.delete(selectedGuest.getDni());
                    DataResponse deleteDataResponse = parseDataResponse(deleteResponse);

                    if (!deleteDataResponse.isSuccess()) {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "No se pudo actualizar el huésped: " + deleteDataResponse.getMessage());
                        return;
                    }


                    Guest updatedGuest = new Guest(
                            nameField.getText().trim(),
                            lastNameField.getText().trim(),
                            newDni,
                            phoneNumber,
                            emailField.getText().trim(),
                            addressField.getText().trim(),
                            nationalityComboBox.getValue()
                    );

                    String createResponse = guestData.create(updatedGuest);
                    DataResponse createDataResponse = parseDataResponse(createResponse);

                    if (!createDataResponse.isSuccess()) {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "No se pudo crear el huésped actualizado: " + createDataResponse.getMessage());
                        return;
                    }
                } else {

                    Guest updatedGuest = new Guest(
                            nameField.getText().trim(),
                            lastNameField.getText().trim(),
                            newDni,
                            phoneNumber,
                            emailField.getText().trim(),
                            addressField.getText().trim(),
                            nationalityComboBox.getValue()
                    );

                    String jsonResponse = guestData.update(updatedGuest);
                    DataResponse response = parseDataResponse(jsonResponse);

                    if (!response.isSuccess()) {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "No se pudo actualizar el huésped: " + response.getMessage());
                        return;
                    }
                }

                loadGuestsFromFile();
                guestTableView.setItems(guestList);

                setFieldsEnabled(false);
                updateButton.setDisable(true);
                editMode = false;


                Guest updatedGuest = new Guest(
                        nameField.getText().trim(),
                        lastNameField.getText().trim(),
                        newDni,
                        phoneNumber,
                        emailField.getText().trim(),
                        addressField.getText().trim(),
                        nationalityComboBox.getValue()
                );

                selectedGuest = updatedGuest;


                for (Guest g : guestList) {
                    if (g.getDni() == newDni) {
                        guestTableView.getSelectionModel().select(g);
                        break;
                    }
                }

                statusLabel.setText("Huésped actualizado con éxito");
                validationLabel.setText("Huésped actualizado correctamente");

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error de Validación",
                        "DNI y Teléfono deben ser números válidos");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Error al actualizar el huésped: " + e.getMessage());
            }
        }
    }


    @FXML
    private void handleClear(ActionEvent event) {
        clearFields();
        guestTableView.getSelectionModel().clearSelection();
        selectedGuest = null;

        setFieldsEnabled(false);
        saveButton.setDisable(true);
        updateButton.setDisable(true);
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        statusLabel.setText("Campos limpiados");
        validationLabel.setText("Complete todos los campos requeridos");
    }

    @FXML
    private void handleClose(ActionEvent event) throws IOException {
        DataFactory.closeAll();

        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void clearFields() {
        nameField.clear();
        lastNameField.clear();
        dniField.clear();
        phoneNumberField.clear();
        emailField.clear();
        addressField.clear();
        nationalityComboBox.setValue("Costa Rica");
    }


    private void setFieldsEnabled(boolean enabled) {
        nameField.setDisable(!enabled);
        lastNameField.setDisable(!enabled);
        dniField.setDisable(!enabled);
        phoneNumberField.setDisable(!enabled);
        emailField.setDisable(!enabled);
        addressField.setDisable(!enabled);
        nationalityComboBox.setDisable(!enabled);
        saveButton.setDisable(!enabled);

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
            errorMessage.append("El email no tiene un formato válido.\n");
        }

        if (addressField.getText().trim().isEmpty()) {
            errorMessage.append("La dirección no puede estar vacía.\n");
        }

        if (nationalityComboBox.getValue() == null || nationalityComboBox.getValue().equals("---")) {
            errorMessage.append("Debe seleccionar una nacionalidad.\n");
        }

        if (errorMessage.length() > 0) {
            validationLabel.setText("❌ " + errorMessage.toString().replace("\n", " "));
            showAlert(Alert.AlertType.ERROR, "Error de Validación", errorMessage.toString());
            return false;
        }

        validationLabel.setText("✅ Todos los campos son válidos");
        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
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
        public void setData(Object data) { this.data =data;}
    }

    public void setSelectedHotel(Hotel selectedHotel2) {

        throw new UnsupportedOperationException("Unimplemented method 'setSelectedHotel'");
    }
}
