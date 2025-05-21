package com.MagicalStay.ui.controllers;

import com.MagicalStay.ui.MainApp;
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

    @FXML
    private void initialize() {
        // Setup clock updating
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

    @FXML
    private void handleConnect(ActionEvent event) {
        try {
            Socket socket = MainApp.createSocket();
            if (socket != null && socket.isConnected()) {
                statusLabel.setText("Estado: Conectado");
                connectionStatusLabel.setText("Conectado al servidor");

                // Switch to MDI container
                welcomePane.setVisible(false);
                mdiContainer.setVisible(true);
            }
        } catch (IOException e) {
            statusLabel.setText("Estado: Error de conexión");
            connectionStatusLabel.setText("Error de conexión: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        // Close the application
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }
}