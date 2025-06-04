module com.magicalstay.progra2 {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind; // Para el soporte básico de Jackson
    requires com.fasterxml.jackson.datatype.jsr310; // Para serialización/deserialización de tipos java.time
    requires java.logging;

    exports com.MagicalStay.client.ui.controllers to javafx.fxml, javafx.graphics;
    exports com.MagicalStay.client.ui to javafx.fxml, javafx.graphics;
    exports com.MagicalStay.server to javafx.graphics;
    exports com.MagicalStay.shared.config;
    exports com.MagicalStay.client.data to javafx.fxml, javafx.graphics;
    exports com.MagicalStay.shared.domain to fasterxml.jackson.databind;

    opens com.MagicalStay.client.ui.controllers to javafx.fxml, com.fasterxml.jackson.databind;
    opens com.MagicalStay.client.ui to javafx.fxml, com.fasterxml.jackson.databind;
    opens com.MagicalStay.server to com.fasterxml.jackson.databind;
    opens com.MagicalStay.shared.config to com.fasterxml.jackson.databind;
    opens com.MagicalStay.client.data to javafx.fxml, com.fasterxml.jackson.databind;
    opens com.MagicalStay.shared.data to com.fasterxml.jackson.databind;
    opens com.MagicalStay.shared.domain to com.fasterxml.jackson.databind;

}