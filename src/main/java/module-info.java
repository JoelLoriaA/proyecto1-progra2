module cr.ac.ucr.paraiso.progra2.c4j816.proyectoprogra2 {
    requires javafx.graphics;
    requires javafx.controls; // Incluye otros módulos de JavaFX si son necesarios
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.logging; // Si usas FXML
    exports com.MagicalStay.ui to javafx.graphics;
}