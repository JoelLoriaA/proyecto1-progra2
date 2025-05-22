package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.ui.MainApp;
import com.MagicalStay.data.ConnectionStatus;
import com.MagicalStay.data.DatabaseClient;
import com.MagicalStay.data.RequestDTO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainPaneController {
    @FXML
    private BorderPane welcomePane;
    @FXML
    private StackPane mdiContainer;
    @FXML
    private Button connectButton;
    @FXML
    private Button exitButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Label connectionStatusLabel;
    @FXML
    private Label dateTimeLabel;

    private Timeline clockTimeline;
    private DatabaseClient databaseClient;
    private Socket socket;

    @FXML
    private void initialize() {
        // Configuración del reloj
        setupClock();
        
        // Inicializar el cliente de base de datos
        databaseClient = new DatabaseClient();
        
        // Estado inicial
        mdiContainer.setVisible(false);
        updateConnectionStatus(false);
    }

    @FXML
    private void handleConnect(ActionEvent event) {
        // Show a loading indicator or disable buttons while connecting
        connectButton.setDisable(true);
        statusLabel.setText("Estado: Conectando...");

        Task<Void> connectionTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    socket = MainApp.createSocket();

                    // Verify database connection
                    RequestDTO testRequest = new RequestDTO("TEST_CONNECTION", null, null);
                    databaseClient.executeRequest(testRequest, ConnectionStatus.class)
                            .thenAcceptAsync(status -> {
                                Platform.runLater(() -> {
                                    if (status.isConnected()) {
                                        updateConnectionStatus(true);
                                        showMainContainer();
                                    } else {
                                        updateConnectionStatus(false);
                                        showError("La base de datos no está disponible");
                                    }
                                });
                            })
                            .exceptionally(throwable -> {
                                Platform.runLater(() -> {
                                    updateConnectionStatus(false);
                                    showError("Error de conexión: " + throwable.getMessage());
                                });
                                return null;
                            });

                } catch (IOException e) {
                    Platform.runLater(() -> {
                        updateConnectionStatus(false);
                        showError("Error de conexión: " + e.getMessage());
                    });
                }
                return null;
            }
        };

        new Thread(connectionTask).start();
    }

    private void updateConnectionStatus(boolean connected) {
        statusLabel.setText("Estado: " + (connected ? "Conectado" : "Desconectado"));
        connectionStatusLabel.setText(connected ? 
            "Conectado al servidor" : 
            "Sin conexión al servidor");
        connectButton.setDisable(connected);
    }

    private void showMainContainer() {
        welcomePane.setVisible(false);
        mdiContainer.setVisible(true);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Conexión");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleExit(ActionEvent event) {
        // Cerrar conexiones antes de salir
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Detener el reloj
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        
        // Cerrar la aplicación
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    private void setupClock() {
        clockTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> updateClock())
        );
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
        updateClock();
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        dateTimeLabel.setText("Fecha: " + formatter.format(now));
    }
}