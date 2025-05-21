package com.MagicalStay;

import com.MagicalStay.domain.Hotel;
import com.MagicalStay.domain.Room;
import com.MagicalStay.domain.RoomCondition;
import com.MagicalStay.domain.RoomType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void Main(String [] args){
        List<Room> rooms = new ArrayList<>();
        Hotel hotel = new Hotel(1, "Hotel Paraíso", "Av. Central 123, Ciudad Fantasía", rooms);

        rooms.add(new Room("101", RoomType.ESTANDAR, RoomCondition.DISPONIBLE,hotel));
        rooms.add(new Room("202", RoomType.SUITE, RoomCondition.EN_MANTENIMIENTO,hotel));

        System.out.println("--- Rooms Grouped by type for " + hotel.getName() + "----");

        Map<RoomType, List<Room>> roomsGroupedByType = hotel.getRooms().stream()
                .collect(Collectors.groupingBy(Room::getRoomType));

      for(Map.Entry<RoomType, List<Room>> entry : roomsGroupedByType.entrySet()){
          RoomType type = entry.getKey();
          List<Room> roomsofType = entry.getValue();
          System.out.println("\nRooms of Type" + type);
          for(Room room : roomsofType){
              System.out.println("  -  " + room);
          }
      }
    }
}