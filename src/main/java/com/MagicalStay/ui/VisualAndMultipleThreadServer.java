package com.MagicalStay.ui;

import com.MagicalStay.sockets.sevidormultihilo.MultiServidorHilo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;

public class VisualAndMultipleThreadServer extends Application {
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        System.out.println(Thread.currentThread().toString());
        Pane pane = new Pane(); //container for shapes

        this.statusLabel = new Label("Servidor activo");
        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(statusLabel, pane);
        Scene scene = new Scene(vBox, 600, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Aplicación con múltiples hilos");
        primaryStage.show();
        primaryStage.sizeToScene();

    }

    private void serverSocketStart(){

        Task<Void> backgroundTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                boolean escuchando = true;

                try {
                    ServerSocket serverSocket = new ServerSocket(9999);
                    System.out.println("Servidor activo");
                    while(escuchando){
                        System.out.println(Thread.currentThread().toString());
                        MultiServidorHilo hilo = new MultiServidorHilo(serverSocket.accept());
                        hilo.run();
                    }
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                return null;
            }
        };
        new Thread(backgroundTask).start();
    }//severStart
    public static void main(String[] args) {
        launch(args);

    }


}
