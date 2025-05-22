package com.MagicalStay.ui.controllers;


import com.MagicalStay.ui.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class MainMenuController {

    @FXML
    private MenuItem connectMenuItem;

    @FXML
    private MenuItem disconnectMenuItem;

    @FXML
    private MenuItem exitMenuItem;

    @FXML
    private MenuItem hotelManagementMenuItem;

    @FXML
    private MenuItem roomManagementMenuItem;
    @FXML
    private MenuItem guestMenuItem;
    @FXML
    private MenuItem newBookingMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private MenuItem receptionistMenuItem;
    @FXML
    private MenuItem searchAvailabilityMenuItem;
    @FXML
    private MenuItem bookingHistoryMenuItem;

    // Initialize method called automatically when FXML is loaded
    @FXML
    private void initialize() {
        // Initialization code if needed
    }

    @FXML
    private void handleConnect(ActionEvent event) {
        try {
            Socket socket = MainApp.createSocket();
            if (socket != null && socket.isConnected()) {
                connectMenuItem.setDisable(true);
                disconnectMenuItem.setDisable(false);
                showAlert(Alert.AlertType.INFORMATION, "Conexión Exitosa",
                        "Se ha establecido conexión con el servidor.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Conexión",
                    "No se pudo conectar al servidor: " + e.getMessage());
        }
    }

    @FXML
    private void handleDisconnect(ActionEvent event) {
        Socket socket = MainApp.getSocket();
        if (socket != null) {
            try {
                socket.close();
                connectMenuItem.setDisable(false);
                disconnectMenuItem.setDisable(true);
                showAlert(Alert.AlertType.INFORMATION, "Desconexión",
                        "Se ha desconectado del servidor.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error de Desconexión",
                        "Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        // Close the application
        Stage stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        stage.close();
    }

    @FXML
    private void handleHotelManagement(ActionEvent event) {
        try {
            // Load the hotel management FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotel-management.fxml"));
            Parent root = loader.load();

            // Create a new stage for the hotel management window
            Stage hotelStage = new Stage();
            hotelStage.setTitle("Gestión de Hoteles");
            hotelStage.setScene(new Scene(root));
            hotelStage.initModality(Modality.NONE); // Allow interaction with other windows
            hotelStage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo cargar la ventana de gestión de hoteles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRoomManagement(ActionEvent event) {
        try {
            // Load the room management FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/room-management.fxml"));
            Parent root = loader.load();

            // Create a new stage for the room management window
            Stage roomStage = new Stage();
            roomStage.setTitle("Gestión de Habitaciones");
            roomStage.setScene(new Scene(root));
            roomStage.initModality(Modality.NONE); // Allow interaction with other windows
            roomStage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo cargar la ventana de gestión de habitaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGuestManagement(ActionEvent event) {
        // To be implemented
        showAlert(Alert.AlertType.INFORMATION, "En Desarrollo",
                "La funcionalidad de gestión de huéspedes está en desarrollo.");
    }

    @FXML
    private void handleReceptionistManagement(ActionEvent event) {
        // To be implemented
        showAlert(Alert.AlertType.INFORMATION, "En Desarrollo",
                "La funcionalidad de gestión de recepcionistas está en desarrollo.");
    }

    @FXML
    private void handleNewBooking(ActionEvent event) {
        // To be implemented
        showAlert(Alert.AlertType.INFORMATION, "En Desarrollo",
                "La funcionalidad de nueva reservación está en desarrollo.");
    }

    @FXML
    private void handleSearchAvailability(ActionEvent event) {
        // To be implemented
        showAlert(Alert.AlertType.INFORMATION, "En Desarrollo",
                "La funcionalidad de búsqueda de disponibilidad está en desarrollo.");
    }

    @FXML
    private void handleBookingHistory(ActionEvent event) {
        // To be implemented
        showAlert(Alert.AlertType.INFORMATION, "En Desarrollo",
                "La funcionalidad de historial de reservaciones está en desarrollo.");
    }

    @FXML
    private void handleAbout(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Acerca de",
                "Sistema de Gestión de Reservaciones MagicalStay\n" +
                        "Versión 1.0\n" +
                        "© 2025 - Todos los derechos reservados");
    }

    // Helper method to show alerts
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void systemOnAction(ActionEvent actionEvent) {
    }
}