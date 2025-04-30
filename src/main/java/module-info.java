module com.pfe.nova {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires itextpdf;
    requires java.desktop;
    requires org.controlsfx.controls;
    requires java.sql;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires static jbcrypt;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires twilio;
    requires org.json;
    requires javafx.web;
    requires java.mail;    // Ouvre les packages pour FXML et JavaFX
    opens com.pfe.nova to javafx.fxml;
    opens com.pfe.nova.Application to javafx.graphics, javafx.fxml;
    opens com.pfe.nova.models to javafx.base, javafx.fxml;
    opens com.pfe.nova.Controller to javafx.fxml;
    opens com.pfe.nova.configuration to javafx.fxml;

    // Exporte les packages (facultatif si besoin externe)
    exports com.pfe.nova;
    exports com.pfe.nova.Application;
    exports com.pfe.nova.Controller;
    exports com.pfe.nova.models;
}
