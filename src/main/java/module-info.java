module com.magicalstay.progra2 {
        requires javafx.graphics;
        requires javafx.controls;
        requires javafx.fxml;
        requires com.fasterxml.jackson.databind;
        requires com.fasterxml.jackson.datatype.jsr310;
        requires java.logging;
        requires com.fasterxml.jackson.core;
        requires jakarta.xml.bind;


        // Exports
        exports com.MagicalStay.client.ui.controllers to javafx.fxml, javafx.graphics;
        exports com.MagicalStay.client.ui to javafx.fxml, javafx.graphics;
        exports com.MagicalStay.server to javafx.graphics;
        exports com.MagicalStay.shared.config;
        exports com.MagicalStay.client.data to javafx.fxml, javafx.graphics;
        exports com.MagicalStay.shared.domain to com.fasterxml.jackson.databind;

        // Opens
        opens com.MagicalStay.client.ui.controllers to javafx.fxml, com.fasterxml.jackson.databind;
        opens com.MagicalStay.client.ui to javafx.fxml, com.fasterxml.jackson.databind;
        opens com.MagicalStay.server to com.fasterxml.jackson.databind;
        opens com.MagicalStay.shared.config to com.fasterxml.jackson.databind;
        opens com.MagicalStay.client.data to javafx.fxml, com.fasterxml.jackson.databind;
        opens com.MagicalStay.shared.data to com.fasterxml.jackson.databind;
        opens com.MagicalStay.shared.domain to com.fasterxml.jackson.databind, javafx.base;

}