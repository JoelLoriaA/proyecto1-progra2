package com.MagicalStay.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;

public class MainMenuController
{
    @javafx.fxml.FXML
    private MenuItem menuExit;
    @javafx.fxml.FXML
    private MenuItem menuMcd;
    @javafx.fxml.FXML
    private MenuItem menuTeoría;

    @javafx.fxml.FXML
    public void initialize() {
    }

    @javafx.fxml.FXML
    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }


    }