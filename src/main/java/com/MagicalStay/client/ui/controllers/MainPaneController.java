package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.sockets.SocketCliente;
import com.MagicalStay.shared.config.ConfiguracionApp;
import com.MagicalStay.shared.domain.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;

public class MainPaneController implements SocketCliente.ClienteCallback {

    @FXML private BorderPane welcomePane;
    @FXML private Button connectButton;
    @FXML private Button exitButton;
    @FXML private Label statusLabel;

    // Nuevos elementos para la interfaz conectada
    @FXML private VBox connectedInterface;
    @FXML private Button hotelManagementButton;
    @FXML private Button roomManagementButton;
    @FXML private Button bookingManagementButton;
    @FXML private Button guestManagementButton;
    @FXML private Button frontDeskManagementButton;
    @FXML private Button disconnectButton;
    @FXML private Label welcomeLabel;
    @FXML private Label connectionStatusLabel;

    private SocketCliente socketCliente;
    private boolean isConnected = false;
    private UserRole currentRole;
    @FXML
    private BorderPane mdiContainer;

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

            // Mostrar diálogo de rol
            showRoleDialog();

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
        openWindow(ConfiguracionApp.FXML_HOTEL_MANAGEMENT, "Gestión de Hoteles");
    }

    @FXML
    private void handleRoomManagement() {
        openWindow(ConfiguracionApp.FXML_ROOM_MANAGEMENT, "Gestión de Habitaciones");
    }

    @FXML
    private void handleBookingManagement() {
        openWindow(ConfiguracionApp.FXML_BOOKING_MANAGEMENT, "Gestión de Reservas");
    }

    @FXML
    private void handleGuestManagement() {
        openWindow(ConfiguracionApp.FXML_GUEST_MANAGEMENT, "Gestión de Huéspedes");
    }

    @FXML
    private void handleFrontDeskManagement() {
        openWindow(ConfiguracionApp.FXML_FRONTDESK_MANAGEMENT, "Gestión de Recepcionistas");
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

    private void showRoleDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Selección de Rol");
        alert.setHeaderText("Por favor, seleccione su rol:");
        alert.setContentText("¿Es usted administrador?");

        ButtonType buttonTypeAdmin = new ButtonType("Administrador");
        ButtonType buttonTypeFrontDesk = new ButtonType("Recepcionista");

        alert.getButtonTypes().setAll(buttonTypeAdmin, buttonTypeFrontDesk);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypeAdmin) {
                currentRole = UserRole.ADMIN;
            } else {
                currentRole = UserRole.FRONTDESK;
            }
            updateUIBasedOnRole();
        });
    }

    private void updateUIBasedOnRole() {
        if (currentRole == UserRole.FRONTDESK) {
            frontDeskManagementButton.setVisible(false);
            frontDeskManagementButton.setManaged(false);
        } else {
            frontDeskManagementButton.setVisible(true);
            frontDeskManagementButton.setManaged(true);
        }
    }
}