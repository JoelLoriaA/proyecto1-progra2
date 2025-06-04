package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.shared.config.ConfiguracionApp;
import com.MagicalStay.shared.domain.UserRole;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
// ... (importaciones existentes)
import com.MagicalStay.client.sockets.SocketCliente;

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

    private final SocketCliente socketCliente;
    private UserRole currentRole;
    @FXML
    private Menu adminMenu;

    @FXML
    private void initialize() {
        // Por defecto, deshabilitar menús hasta que se establezca el rol
        habilitarMenusOperacion(false);

        // Asegurarse de que el menú de administración esté oculto para recepcionistas
        if (currentRole == UserRole.FRONTDESK) {
            updateMenusBasedOnRole();
        }
    }

    public MainMenuController() {
        socketCliente = new SocketCliente(new SocketCliente.ClienteCallback() {
            @Override
            public void onMensajeRecibido(String mensaje) {
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Mensaje", mensaje));
            }

            @Override
            public void onError(String error) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", error));
            }

            @Override
            public void onConexionEstablecida() {
                Platform.runLater(() -> {
                    connectMenuItem.setDisable(true);
                    disconnectMenuItem.setDisable(false);
                    habilitarMenusOperacion(true);
                });
            }

            @Override
            public void onDesconexion() {
                Platform.runLater(() -> {
                    connectMenuItem.setDisable(false);
                    disconnectMenuItem.setDisable(true);
                    habilitarMenusOperacion(false);
                });
            }
        });
    }

    private void updateMenusBasedOnRole() {
        if (currentRole == UserRole.FRONTDESK) {
            // Ocultar elementos administrativos para recepcionistas
            if (hotelManagementMenuItem != null && hotelManagementMenuItem.getParentMenu() != null) {
                Menu adminMenu = hotelManagementMenuItem.getParentMenu();
                adminMenu.setVisible(false);
                adminMenu.setVisible(false);
            }

            // Deshabilitar ítems específicos
            hotelManagementMenuItem.setVisible(false);
            roomManagementMenuItem.setVisible(false);
            receptionistMenuItem.setVisible(false);

            // Mantener visibles solo los ítems permitidos para recepcionistas
            guestMenuItem.setVisible(true);
            newBookingMenuItem.setVisible(true);
            searchAvailabilityMenuItem.setVisible(true);
            bookingHistoryMenuItem.setVisible(true);
        } else {
            // Para administradores, mostrar todo
            if (hotelManagementMenuItem != null && hotelManagementMenuItem.getParentMenu() != null) {
                Menu adminMenu = hotelManagementMenuItem.getParentMenu();
                adminMenu.setVisible(true);
                adminMenu.setVisible(true);
            }

            // Habilitar todos los ítems
            hotelManagementMenuItem.setVisible(true);
            roomManagementMenuItem.setVisible(true);
            receptionistMenuItem.setVisible(true);
            guestMenuItem.setVisible(true);
            newBookingMenuItem.setVisible(true);
            searchAvailabilityMenuItem.setVisible(true);
            bookingHistoryMenuItem.setVisible(true);
        }
    }

    private void habilitarMenusOperacion(boolean habilitar) {
        if (currentRole == UserRole.FRONTDESK) {
            // Para recepcionistas, solo habilitar menús permitidos
            guestMenuItem.setDisable(!habilitar);
            newBookingMenuItem.setDisable(!habilitar);
            searchAvailabilityMenuItem.setDisable(!habilitar);
            bookingHistoryMenuItem.setDisable(!habilitar);

            // Mantener deshabilitados los menús administrativos
            hotelManagementMenuItem.setDisable(true);
            roomManagementMenuItem.setDisable(true);
            receptionistMenuItem.setDisable(true);
        } else {
            // Para administradores, habilitar todos los menús
            hotelManagementMenuItem.setDisable(!habilitar);
            roomManagementMenuItem.setDisable(!habilitar);
            guestMenuItem.setDisable(!habilitar);
            receptionistMenuItem.setDisable(!habilitar);
            newBookingMenuItem.setDisable(!habilitar);
            searchAvailabilityMenuItem.setDisable(!habilitar);
            bookingHistoryMenuItem.setDisable(!habilitar);
        }
    }

    @FXML
    private void handleConnect() {
        socketCliente.conectar(ConfiguracionApp.HOST_SERVIDOR, ConfiguracionApp.PUERTO_SERVIDOR);
    }

    @FXML
    private void handleDisconnect() {
        socketCliente.desconectar();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/MagicalStay/hotel-management.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/MagicalStay/room-management.fxml"));
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
}