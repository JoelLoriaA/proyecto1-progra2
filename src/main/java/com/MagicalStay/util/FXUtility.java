package com.MagicalStay.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class FXUtility {

    public static void loadPage(String className, String page, BorderPane bp) {
        try {
            Class cl = Class.forName(className);
            FXMLLoader fxmlLoader = new FXMLLoader(cl.getResource(page));
            cl.getResource("bp");
            bp.setCenter(fxmlLoader.load());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadPage2(String className, String page, AnchorPane pantalla) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(Class.forName(className).getResource(page));
            AnchorPane pageContent = fxmlLoader.load();
            pantalla.getChildren().clear();
            pantalla.getChildren().add(pageContent);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al cargar el archivo FXML", e);
        }
    }

    public static Alert alertError(String title, String header){
        Alert myAlert = new Alert(Alert.AlertType.ERROR);
        myAlert.setTitle(title);
        myAlert.setHeaderText(header);
        myAlert.setContentText(" ");

        // Aplicar estilos personalizados
        DialogPane dialogPane = myAlert.getDialogPane();
        dialogPane.getStylesheets().add(
            FXUtility.class.getResource("/ucr/lab/stylesheet.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("error-alert");
        
        return myAlert;
    }

    public static Alert alertInformation(String title, String header){
        Alert myAlert = new Alert(Alert.AlertType.INFORMATION);
        myAlert.setTitle(title);
        myAlert.setHeaderText(header);
        myAlert.setContentText(" ");

        // Aplicar estilos personalizados
        DialogPane dialogPane = myAlert.getDialogPane();
        dialogPane.getStylesheets().add(
            FXUtility.class.getResource("/ucr/lab/stylesheet.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("custom-alert");
        
        return myAlert;
    }

    public static TextInputDialog dialog(String title, String header){
        TextInputDialog dialog = new TextInputDialog(title);
        dialog.setHeaderText(header);


        // Aplicar estilos personalizados
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
            FXUtility.class.getResource("/ucr/lab/alert-style.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("custom-alert");
        
        return dialog;
    }
}