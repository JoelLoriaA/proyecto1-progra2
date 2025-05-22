package com.MagicalStay.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class MainApp extends Application {

    private static BorderPane root = new BorderPane();
    private static Socket socket = null;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Usar getResourceAsStream para verificar si los recursos existen
            MenuBar menuBar = FXMLLoader.load(
                MainApp.class.getResource("/main-menubar.fxml"));
            
            AnchorPane anchorPane = FXMLLoader.load(
                MainApp.class.getResource("/main-pane.fxml"));

            if (menuBar == null || anchorPane == null) {
                throw new IOException("No se pudieron cargar los archivos FXML");
            }

            root.setTop(menuBar);
            root.setCenter(anchorPane);
            Scene scene = new Scene(root);

            primaryStage.setTitle("Múltiples formularios");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Crear una interfaz básica en caso de error
            createFallbackUI(primaryStage);
        }
    }

    private void createFallbackUI(Stage primaryStage) {
        BorderPane fallbackRoot = new BorderPane();
        Scene fallbackScene = new Scene(fallbackRoot, 800, 600);
        primaryStage.setScene(fallbackScene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Socket createSocket() throws IOException {
        if (socket == null) {
            socket = new Socket("192.168.56.1", 99999);
        }
        return socket;
    }

    public static Socket getSocket() {
        return socket;
    }
}