package com.MagicalStay.client.ui;

import com.MagicalStay.shared.config.ConfiguracionApp;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.net.URL; // Import URL

public class MainApp extends Application {

    private static BorderPane root = new BorderPane();
    private static Socket socket = null;

    @Override
    public void start(Stage primaryStage) {
        try {

            // Usar getResource para obtener la URL correcta
            URL menuBarUrl = MainApp.class.getResource("/com/MagicalStay/main-menubar.fxml");
            URL anchorPaneUrl = MainApp.class.getResource("/com/MagicalStay/main-pane.fxml");

            if (menuBarUrl == null || anchorPaneUrl == null) {
                throw new IOException("No se pudieron encontrar los archivos FXML. Verifique las rutas.");
            }

            MenuBar menuBar = FXMLLoader.load(menuBarUrl);
            AnchorPane anchorPane = FXMLLoader.load(anchorPaneUrl);

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
            socket = new Socket(ConfiguracionApp.HOST_SERVIDOR, ConfiguracionApp.PUERTO_SERVIDOR);// Usar el puerto definido en ServerApp
        }
        return socket;
    }

    public static Socket getSocket() {
        return socket;
    }
}