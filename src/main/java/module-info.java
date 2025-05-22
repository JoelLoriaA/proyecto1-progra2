module com.MagicalStay.progra2 {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.logging;

    exports com.MagicalStay.ui to javafx.graphics;
    exports com.MagicalStay.ui.controllers to javafx.fxml;

    opens com.MagicalStay.ui.controllers to javafx.fxml;
}