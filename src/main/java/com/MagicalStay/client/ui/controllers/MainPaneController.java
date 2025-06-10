package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.client.sockets.SocketCliente;
import com.MagicalStay.shared.config.ConfiguracionApp;
import com.MagicalStay.shared.data.JsonResponse;
import com.MagicalStay.shared.domain.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.util.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Optional;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    // Java
    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setOnHidden(event -> {
                new Thread(() -> {
                    if (socketCliente.estaConectado()) {
                        try {
                            socketCliente.detenerEscuchaMensajes();
                            socketCliente.iniciarSincronizacionBidireccional();
                            Platform.runLater(() -> socketCliente.iniciarEscuchaMensajes());
                        } catch (Exception e) {
                            Platform.runLater(() -> showAlert("Error", "Error en sincronización", e.getMessage(), Alert.AlertType.ERROR));
                            // No desconectes aquí, solo muestra el error
                        }
                    } else {
                        Platform.runLater(() -> showAlert("Advertencia", "Desconectado", "No se puede sincronizar porque la conexión se ha perdido.", Alert.AlertType.WARNING));
                    }
                }).start();
            });
            stage.show();

        } catch (IOException e) {
            showAlert("Error", "No se pudo abrir la ventana: " + title,
                    "Error: " + e.getMessage(), Alert.AlertType.ERROR);
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
        Platform.runLater(() -> {
            statusLabel.setText("Error: " + error);
            connectButton.setDisable(false);
            updateConnectionStatus(false);

            // Usar Alert directamente en lugar de showAlert()
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Conexión");
            alert.setHeaderText("No se pudo conectar al servidor");
            alert.setContentText(error);
            alert.show(); // Usar show() en lugar de showAndWait()
        });
    }

    @Override
    public void onConexionEstablecida() {
        Platform.runLater(() -> {
            statusLabel.setText("Conectado al servidor");
            updateConnectionStatus(true);

            if (connectionStatusLabel != null) {
                connectionStatusLabel.setText("Conectado");
            }

            // Sincronizar archivos primero, luego iniciar escucha
            new Thread(() -> {
                try {
                    socketCliente.iniciarSincronizacionBidireccional();
                    Platform.runLater(() -> socketCliente.iniciarEscuchaMensajes());
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert("Error", "Error en sincronización", e.getMessage(), Alert.AlertType.ERROR);
                        updateConnectionStatus(false);
                    });
                }
            }).start();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Conexión Exitosa");
            alert.setHeaderText("¡Conectado al servidor!");
            alert.setContentText("La conexión se ha establecido correctamente.");
            alert.show();
        });
    }



    @Override
    public void onDesconexion() {
        statusLabel.setText("Desconectado del servidor");
        updateConnectionStatus(false);

        if (connectionStatusLabel != null) {
            connectionStatusLabel.setText("Estado: Desconectado");
        }
    }

    // Cambia showRoleDialog para que devuelva boolean
    private boolean showRoleDialog() {
        try {
            String jsonResponse = DataFactory.getFrontDeskData().retrieveAll();
            JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);
            boolean hasReceptionists = response.isSuccess() && response.getData() != null;

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Selección de Rol");

            if (!hasReceptionists) {
                alert.setHeaderText("No hay recepcionistas registrados.");
                alert.setContentText("Debe ingresar como administrador para registrar recepcionistas.");
                alert.getButtonTypes().setAll(ButtonType.OK);
                alert.showAndWait();
                currentRole = UserRole.ADMIN;
                updateUIBasedOnRole();
                return true;
            }

            alert.setHeaderText("Seleccione su rol:");
            ButtonType buttonTypeAdmin = new ButtonType("Administrador");
            ButtonType buttonTypeFrontDesk = new ButtonType("Recepcionista");
            alert.getButtonTypes().setAll(buttonTypeAdmin, buttonTypeFrontDesk);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == buttonTypeAdmin) {
                    currentRole = UserRole.ADMIN;
                    updateUIBasedOnRole();
                    return true;
                } else if (result.get() == buttonTypeFrontDesk) {
                    return showLoginDialog();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error al verificar recepcionistas",
                    e.getMessage(), Alert.AlertType.ERROR);
        }
        return false;
    }

    // Cambia showLoginDialog para que devuelva boolean
    private boolean showLoginDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Inicio de Sesión");
        dialog.setHeaderText("Ingrese sus credenciales de recepcionista");

        ButtonType loginButtonType = new ButtonType("Iniciar Sesión", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Usuario");
        PasswordField password = new PasswordField();
        password.setPromptText("Contraseña");

        grid.add(new Label("Usuario:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(password, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                String jsonResponse = DataFactory.getFrontDeskData().authenticate(
                        result.get().getKey(), result.get().getValue());
                JsonResponse response = objectMapper.readValue(jsonResponse, JsonResponse.class);

                if (response.isSuccess()) {
                    currentRole = UserRole.FRONTDESK;
                    updateUIBasedOnRole();
                    return true;
                } else {
                    showAlert("Error", "Error de Autenticación", response.getMessage(), Alert.AlertType.ERROR);
                    return showLoginDialog(); // Vuelve a pedir login
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Error de Autenticación", e.getMessage(), Alert.AlertType.ERROR);
                return showLoginDialog();
            }
        } else {
            // Si cancela, vuelve a mostrar el diálogo de rol
            return false;
        }
    }

    // Modifica handleConnect para solo conectar si el rol fue seleccionado/autenticado
    @FXML
    private void handleConnect() {
        if (!isConnected) {
            statusLabel.setText("Conectando al servidor...");
            connectButton.setDisable(true);

            boolean rolSeleccionado = showRoleDialog();

            if (rolSeleccionado) {
                socketCliente.conectar(ConfiguracionApp.HOST_SERVIDOR, ConfiguracionApp.PUERTO_SERVIDOR);
            } else {
                statusLabel.setText("Conexión cancelada.");
                connectButton.setDisable(false);
            }
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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