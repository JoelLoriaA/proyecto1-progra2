package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.sockets.SocketCliente;
import com.MagicalStay.shared.config.ConfiguracionApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainPaneController implements SocketCliente.ClienteCallback {
    @FXML private BorderPane welcomePane;
    @FXML private StackPane mdiContainer;
    @FXML private Button connectButton;
    @FXML private Button exitButton;
    @FXML private Label statusLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private Label dateTimeLabel;

    private Timeline clockTimeline;
    private SocketCliente socketCliente;
    private static final int MAX_REINTENTOS = 3;
    private int intentosConexion = 0;
    private Timeline reconexionTimeline;

    @FXML
    private void initialize() {
        setupClock();
        setupSocketCliente();
        mdiContainer.setVisible(false);
        updateConnectionStatus(false);
    }

    private void setupSocketCliente() {
        socketCliente = new SocketCliente(this);
    }

    @FXML
    private void handleConnect(ActionEvent event) {
        intentosConexion = 0;
        conectarAlServidor();
    }

    private void conectarAlServidor() {
        connectButton.setDisable(true);
        statusLabel.setText("Estado: Conectando... Intento " + (intentosConexion + 1) + " de " + MAX_REINTENTOS);

        new Thread(() -> {
            try {
                socketCliente.conectar(
                    ConfiguracionApp.HOST_SERVIDOR, 
                    ConfiguracionApp.PUERTO_SERVIDOR
                );
            } catch (Exception e) {
                Platform.runLater(() -> manejarErrorConexion(e.getMessage()));
            }
        }).start();
    }

    private void manejarErrorConexion(String error) {
        if (intentosConexion < MAX_REINTENTOS) {
            intentosConexion++;
            programarReconexion();
        } else {
            updateConnectionStatus(false);
            showErrorConReintentar("Error de conexión: " + error);
            connectButton.setDisable(false);
        }
    }

    private void programarReconexion() {
        if (reconexionTimeline != null) {
            reconexionTimeline.stop();
        }
        
        reconexionTimeline = new Timeline(
            new KeyFrame(Duration.seconds(5), e -> conectarAlServidor())
        );
        reconexionTimeline.play();
    }

    private void showErrorConReintentar(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Conexión");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        ButtonType retryButton = new ButtonType("Reintentar");
        alert.getButtonTypes().add(retryButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == retryButton) {
            intentosConexion = 0;
            conectarAlServidor();
        }
    }

    // Implementación de SocketCliente.ClienteCallback
    @Override
    public void onMensajeRecibido(String mensaje) {
        Platform.runLater(() -> {
            // Procesar mensajes del servidor
            System.out.println("Mensaje del servidor: " + mensaje);
        });
    }

    @Override
    public void onError(String error) {
        Platform.runLater(() -> {
            showError(error);
            updateConnectionStatus(false);
        });
    }

    @Override
    public void onConexionEstablecida() {
        Platform.runLater(() -> {
            updateConnectionStatus(true);
            showMainContainer();
            if (reconexionTimeline != null) {
                reconexionTimeline.stop();
            }
        });
    }

    @Override
    public void onDesconexion() {
        Platform.runLater(() -> {
            updateConnectionStatus(false);
            welcomePane.setVisible(true);
            mdiContainer.setVisible(false);
            connectButton.setDisable(false);
        });
    }

    private void updateConnectionStatus(boolean connected) {
        statusLabel.setText("Estado: " + (connected ? "Conectado" : "Desconectado"));
        connectionStatusLabel.setText(connected ? "Conectado al servidor" : "Sin conexión al servidor");
        connectButton.setDisable(connected);
    }

    private void showMainContainer() {
        welcomePane.setVisible(false);
        mdiContainer.setVisible(true);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleExit(ActionEvent event) {
        if (socketCliente != null) {
            socketCliente.desconectar();
        }
        
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        
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