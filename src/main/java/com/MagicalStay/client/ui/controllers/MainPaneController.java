package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.sockets.SocketCliente;
import com.MagicalStay.shared.config.ConfiguracionApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;

public class MainPaneController implements SocketCliente.ClienteCallback {

    @FXML private BorderPane welcomePane;
    @FXML private Button connectButton;
    @FXML private Button exitButton;
    @FXML private Label statusLabel;

    private SocketCliente socketCliente;
    private boolean isConnected = false;
    @FXML
    private Label connectionStatusLabel;
    @FXML
    private Button disconnectButton;
    @FXML
    private BorderPane mdiContainer;
    @FXML
    private Button guestManagementButton;
    @FXML
    private Button hotelManagementButton;
    @FXML
    private Button bookingManagementButton;
    @FXML
    private Label welcomeLabel;
    @FXML
    private VBox connectedInterface;
    @FXML
    private Button reportsButton;
    @FXML
    private Button roomManagementButton;

    @FXML
    private void initialize() {
        socketCliente = new SocketCliente(this);
        updateConnectionStatus(false);

        // Debug: verificar que los elementos FXML se cargaron
        System.out.println("welcomePane: " + (welcomePane != null));
        System.out.println("mdiContainer: " + (mdiContainer != null));
        System.out.println("connectButton: " + (connectButton != null));
        System.out.println("statusLabel: " + (statusLabel != null));
    }

    @FXML
    private void handleConnect() {
        if (!isConnected) {
            statusLabel.setText("Conectando al servidor...");
            connectButton.setDisable(true);

            // Intentar conexión al servidor
            socketCliente.conectar(ConfiguracionApp.HOST_SERVIDOR, ConfiguracionApp.PUERTO_SERVIDOR);
        }
    }

    @FXML
    private void handleExit() {
        if (isConnected) {
            socketCliente.desconectar();
        }
        Platform.exit();
    }

    @FXML
    private void handleDisconnect() {
        socketCliente.desconectar();
    }

    @FXML
    private void handleHotelManagement() {
        openWindow("/com/MagicalStay/hotel-management.fxml", "Gestión de Hoteles");
    }

    @FXML
    private void handleRoomManagement() {
        openWindow("/com/MagicalStay/room-management.fxml", "Gestión de Habitaciones");
    }

    @FXML
    private void handleBookingManagement() {
        openWindow("/com/MagicalStay/booking-management.fxml", "Gestión de Reservas");
    }

    @FXML
    private void handleGuestManagement() {
        openWindow("/com/MagicalStay/guest-management.fxml", "Gestión de Huéspedes");
    }

    @FXML
    private void handleReports() {
        openWindow("/com/MagicalStay/reports.fxml", "Reportes");
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert("Error", "No se pudo abrir la ventana: " + title,
                    "Error: " + e.getMessage());
        }
    }

    private void updateConnectionStatus(boolean connected) {
        this.isConnected = connected;

        System.out.println("Actualizando estado de conexión: " + connected);

        if (connected) {
            // Mostrar interfaz conectada
            if (welcomePane != null) {
                welcomePane.setVisible(false);
                System.out.println("welcomePane ocultado");
            }
            if (mdiContainer != null) {
                mdiContainer.setVisible(true);
                System.out.println("mdiContainer mostrado");
            }

        } else {
            // Mostrar interfaz de bienvenida
            if (welcomePane != null) {
                welcomePane.setVisible(true);
                System.out.println("welcomePane mostrado");
            }
            if (mdiContainer != null) {
                mdiContainer.setVisible(false);
                System.out.println("mdiContainer ocultado");
            }
            if (connectButton != null) {
                connectButton.setDisable(false);
            }
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Implementación de SocketCliente.ClienteCallback
    @Override
    public void onMensajeRecibido(String mensaje) {
        System.out.println("Mensaje del servidor: " + mensaje);

        if (welcomeLabel != null) {
            welcomeLabel.setText("¡Bienvenido al Sistema de Reservas!");
        }
    }

    @Override
    public void onError(String error) {
        statusLabel.setText("Error: " + error);
        connectButton.setDisable(false);
        updateConnectionStatus(false);

        showAlert("Error de Conexión", "No se pudo conectar al servidor", error);
    }

    @Override
    public void onConexionEstablecida() {
        statusLabel.setText("Conectado al servidor");
        updateConnectionStatus(true);

        if (connectionStatusLabel != null) {
            connectionStatusLabel.setText("Estado: Servidor Conectado");
        }

        showAlert("Conexión Exitosa", "¡Conectado al servidor!",
                "La conexión se ha establecido correctamente.");
    }

    @Override
    public void onDesconexion() {
        statusLabel.setText("Desconectado del servidor");
        updateConnectionStatus(false);

        if (connectionStatusLabel != null) {
            connectionStatusLabel.setText("Estado: Desconectado");
        }
    }
}