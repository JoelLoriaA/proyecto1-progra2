package com.MagicalStay;

import com.MagicalStay.domain.Room;
import com.MagicalStay.domain.RoomCondition;
import com.MagicalStay.domain.RoomType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Crear una habitación
        Room room1 = new Room("101", RoomType.ESTANDAR, RoomCondition.DISPONIBLE);
        Room room2 = new Room("202", RoomType.SUITE, RoomCondition.EN_MANTENIMIENTO);

        // Mostrar información
        System.out.println(room1);
        System.out.println(room2);

        // Cambiar condición de la habitación
        room1.setRoomCondition(RoomCondition.OCUPADA);
        System.out.println("Después de actualizar:");
        System.out.println(room1);
        launch();
    }
}