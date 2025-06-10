package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.shared.data.FrontDeskData;
import com.MagicalStay.shared.domain.FrontDeskClerk;
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

public class FrontDeskClerkController {

    @FXML private TableView<FrontDeskClerk> clerkTableView;
    @FXML private TableColumn<FrontDeskClerk, String> nameColumn;
    @FXML private TableColumn<FrontDeskClerk, String> lastNamesColumn;
    @FXML private TableColumn<FrontDeskClerk, String> employeeIdColumn;
    @FXML private TextField nameField;
    @FXML private TextField lastNamesField;
    @FXML private TextField employeeIdField;
    @FXML private TextField dniField;
    @FXML private PasswordField passwordField;
    @FXML private TextField searchTextField;
    @FXML private ComboBox<String> searchTypeComboBox;
    @FXML private Label statusLabel;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button closeButton;
    private FrontDeskData frontDeskData;
    private ObjectMapper objectMapper;
    private ObservableList<FrontDeskClerk> clerkList;
    private FrontDeskClerk selectedClerk;
    private boolean editMode = false;
    @FXML
    private TextField usernameField;
    @FXML
    private TableColumn usernameColumn;
    @FXML
    private Button cancelButton;
    @FXML
    private TableColumn dniColumn;
    @FXML private TextField phoneNumberField;
    @FXML private TableColumn<FrontDeskClerk, Integer> phoneColumn;

    @FXML
    private void initialize() {
        try {
            frontDeskData = DataFactory.getFrontDeskData();
            objectMapper = new ObjectMapper();
            setupTable();
            setupSearchControls();
            setFieldsEnabled(false);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            saveButton.setDisable(true);
        } catch (IOException e) {
            showError("Error al inicializar: " + e.getMessage());
        }
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lastNamesColumn.setCellValueFactory(new PropertyValueFactory<>("lastNames"));
        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        dniColumn.setCellValueFactory(new PropertyValueFactory<FrontDeskClerk, Long>("dni"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<FrontDeskClerk, String>("username"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        loadClerksFromFile();
    }

    private void setupSearchControls() {
        searchTypeComboBox.setItems(FXCollections.observableArrayList(
            "Por Nombre",
            "Por ID Empleado",
            "Por DNI",
            "Todos"
        ));
        searchTypeComboBox.setValue("Por Nombre");
    }

    private void loadClerksFromFile() {
        try {
            String jsonResponse = frontDeskData.retrieveAll();
            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                List<FrontDeskClerk> clerks = objectMapper.convertValue(
                    response.getData(),
                    new TypeReference<List<FrontDeskClerk>>() {}
                );
                clerkList = FXCollections.observableArrayList(clerks);
                clerkTableView.setItems(clerkList);
                showSuccess("Se cargaron " + clerks.size() + " recepcionistas");
            } else {
                clerkList = FXCollections.observableArrayList();
                clerkTableView.setItems(clerkList);
                showError("No se encontraron recepcionistas: " + response.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al cargar recepcionistas: " + e.getMessage());
        }
    }

   @FXML
    private void handleClerkSelection(MouseEvent event) {
        selectedClerk = clerkTableView.getSelectionModel().getSelectedItem();
        if (selectedClerk != null) {
            nameField.setText(selectedClerk.getName());
            lastNamesField.setText(selectedClerk.getLastNames());
            employeeIdField.setText(selectedClerk.getEmployeeId());
            dniField.setText(String.valueOf(selectedClerk.getDni()));
            usernameField.setText(selectedClerk.getUsername());
            phoneNumberField.setText(String.valueOf(selectedClerk.getPhoneNumber()));
            passwordField.clear();
            editButton.setDisable(false);
            deleteButton.setDisable(false);
            setFieldsEnabled(false);
        }
    }

    @FXML
    private void handleAdd() {
        clearFields();
        setFieldsEnabled(true);
        editMode = false;
        saveButton.setDisable(false);
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @FXML
    private void handleEdit() {
        if (selectedClerk != null) {
            setFieldsEnabled(true);
            editMode = true;
            saveButton.setDisable(false);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedClerk != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setContentText("¿Está seguro de eliminar al recepcionista " +
                               selectedClerk.getName() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String jsonResponse = frontDeskData.delete(selectedClerk.getEmployeeId());
                    DataResponse response = parseDataResponse(jsonResponse);

                    if (response.isSuccess()) {
                        loadClerksFromFile();
                        clearFields();
                        showSuccess("Recepcionista eliminado con éxito");
                    } else {
                        showError("No se pudo eliminar: " + response.getMessage());
                    }
                } catch (Exception e) {
                    showError("Error al eliminar: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) return;

        try {
            FrontDeskClerk clerk = new FrontDeskClerk(
                nameField.getText(),
                lastNamesField.getText(),
                employeeIdField.getText(),
                Integer.parseInt(phoneNumberField.getText()),
                Long.parseLong(dniField.getText()),
                usernameField.getText(),
                passwordField.getText()
            );

            String jsonResponse;
            if (editMode) {
                jsonResponse = frontDeskData.update(clerk);
            } else {
                jsonResponse = frontDeskData.create(clerk);
            }

            DataResponse response = parseDataResponse(jsonResponse);
            if (response.isSuccess()) {
                loadClerksFromFile();
                clearFields();
                setFieldsEnabled(false);
                saveButton.setDisable(true);
                showSuccess("Recepcionista guardado con éxito");
            } else {
                showError("Error al guardar: " + response.getMessage());
            }
        } catch (Exception e) {
            showError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        setFieldsEnabled(false);
        saveButton.setDisable(true);
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @FXML
    private void handleClose() throws IOException {
        DataFactory.closeAll();
        ((Stage) closeButton.getScene().getWindow()).close();
    }

    private void clearFields() {
        nameField.clear();
        lastNamesField.clear();
        employeeIdField.clear();
        dniField.clear();
        usernameField.clear();
        passwordField.clear();
        phoneNumberField.clear();
        selectedClerk = null;
    }

    private void setFieldsEnabled(boolean enabled) {
        nameField.setDisable(!enabled);
        lastNamesField.setDisable(!enabled);
        employeeIdField.setDisable(!enabled);
        phoneNumberField.setDisable(!enabled);
        dniField.setDisable(!enabled);
        usernameField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
    }

    private boolean validateFields() {
        String errorMessage = "";

        if (nameField.getText().trim().isEmpty()) {
            errorMessage += "El nombre es requerido\n";
        }
        if (lastNamesField.getText().trim().isEmpty()) {
            errorMessage += "Los apellidos son requeridos\n";
        }
        if (employeeIdField.getText().trim().isEmpty()) {
            errorMessage += "El ID de empleado es requerido\n";
        }
        if (dniField.getText().trim().isEmpty()) {
            errorMessage += "El DNI es requerido\n";
        } else {
            try {
                Integer.parseInt(dniField.getText().trim());
            } catch (NumberFormatException e) {
                errorMessage += "El DNI debe ser un número válido\n";
            }
        }
        if (usernameField.getText().trim().isEmpty()) {
            errorMessage += "El nombre de usuario es requerido\n";
        }
        if (!editMode && passwordField.getText().trim().isEmpty()) {
            errorMessage += "La contraseña es requerida\n";
        }

        if (phoneNumberField.getText().trim().isEmpty()) {
            errorMessage += "El teléfono es requerido\n";
        } else {
            try {
                Integer.parseInt(phoneNumberField.getText().trim());
            } catch (NumberFormatException e) {
                errorMessage += "El teléfono debe ser un número válido\n";
            }
        }

        if (!errorMessage.isEmpty()) {
            showError(errorMessage);
            return false;
        }

        return true;
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
    private void handleCancel() {
        clearFields();
        setFieldsEnabled(false);
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        selectedClerk = null;
        editMode = false;
        statusLabel.setText("Operación cancelada");
    }

    @FXML
    private void handleSearch() {
        try {
            String searchText = searchTextField.getText().trim();
            String searchType = searchTypeComboBox.getValue();
            String jsonResponse;

            if (searchText.isEmpty() || searchType == null) {
                loadClerksFromFile();
                return;
            }

            switch (searchType) {
                case "Por Nombre":
                    jsonResponse = frontDeskData.retrieveByName(searchText);
                    break;
                case "Por ID Empleado":
                    jsonResponse = frontDeskData.retrieveById(searchText);
                    break;
                case "Por DNI":
                    try {
                        long dni = Long.parseLong(searchText);
                        jsonResponse = frontDeskData.retrieveByDni(dni);
                    } catch (NumberFormatException e) {
                        showError("El DNI debe ser un número válido");
                        return;
                    }
                    break;
                case "Todos":
                    loadClerksFromFile();
                    return;
                default:
                    showError("Tipo de búsqueda no válido");
                    return;
            }

            DataResponse response = parseDataResponse(jsonResponse);
            if (response.isSuccess()) {
                if (response.getData() instanceof List) {
                    List<FrontDeskClerk> clerks = objectMapper.convertValue(
                        response.getData(),
                        new TypeReference<List<FrontDeskClerk>>() {}
                    );
                    clerkTableView.setItems(FXCollections.observableArrayList(clerks));
                    showSuccess("Se encontraron " + clerks.size() + " resultados");
                } else {
                    FrontDeskClerk clerk = objectMapper.convertValue(
                        response.getData(),
                        FrontDeskClerk.class
                    );
                    clerkTableView.setItems(FXCollections.observableArrayList(clerk));
                    showSuccess("Se encontró 1 resultado");
                }
            } else {
                clerkTableView.setItems(FXCollections.observableArrayList());
                showError("No se encontraron resultados: " + response.getMessage());
            }

        } catch (Exception e) {
            showError("Error al realizar la búsqueda: " + e.getMessage());
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
}
