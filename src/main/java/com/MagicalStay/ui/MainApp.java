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
    public void start(Stage primaryStage) throws Exception {
        MenuBar menuBar = (MenuBar)
                FXMLLoader.load(getClass().getResource("/main-menubar.fxml"));

        AnchorPane anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/main-pane.fxml"));
        root.setTop(menuBar);
        root.setCenter(anchorPane);
        Scene scene = new Scene(root);

        primaryStage.setTitle("Múltiples formularios");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }


    public static Socket createSocket() throws IOException {
        if (socket == null){
//            InetAddress inetAddress = InetAddress.getLocalHost();
             socket = new Socket("192.168.100.144", 99999);
        }
        return socket;
    }

    public static Socket getSocket() {
        return socket;
    }
}
