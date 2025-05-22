package com.MagicalStay.ui.controllers;

import com.MagicalStay.data.GuestData;
import com.MagicalStay.domain.Guest;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;

public class GuestManagementController {
    private GuestData guestData;

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

    public void setGuestData(GuestData guestData) {
        this.guestData = guestData;
    }

    @FXML
    private void handleSave() {
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
            guestData.create(guest);
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Huésped guardado correctamente");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar el huésped: " + e.getMessage());
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