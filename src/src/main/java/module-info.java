module com.magicalstay.progra2 {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.logging;

    exports com.MagicalStay.client.ui.controllers to javafx.fxml, javafx.graphics;
    exports com.MagicalStay.client.ui to javafx.fxml, javafx.graphics;
    exports com.MagicalStay.server to javafx.graphics;
    
    opens com.MagicalStay.client.ui.controllers to javafx.fxml;
    opens com.MagicalStay.client.ui to javafx.fxml;
}