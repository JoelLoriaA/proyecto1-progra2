package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.shared.data.FrontDeskData;
import com.MagicalStay.shared.domain.FrontDeskClerk;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;

public class ReceptionistManagementController {
    private FrontDeskData frontDeskData;

    @FXML
    private TextField nameField;
    @FXML
    private TextField lastNamesField;
    @FXML
    private TextField employeeIdField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField userField;
    @FXML
    private PasswordField passwordField;

    public void setFrontDeskData(FrontDeskData frontDeskData) {
        this.frontDeskData = frontDeskData;
    }

    @FXML
    private void handleSave() {
        try {
            FrontDeskClerk clerk = new FrontDeskClerk(
                nameField.getText(),
                lastNamesField.getText(),
                employeeIdField.getText(),
                Integer.parseInt(phoneField.getText()),
                userField.getText(),
                passwordField.getText()
            );
            frontDeskData.create(clerk);
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Recepcionista guardado correctamente");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar el recepcionista: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}