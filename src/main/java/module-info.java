module com.pfe.nova {
    // JavaFX
    requires javafx.fxml;
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
    requires com.google.api.client;
    requires google.api.services.oauth2.v2.rev157;
    requires com.google.api.client.json.gson;
    requires google.api.client;
    requires okhttp3;
    requires java.net.http;
    requires jjwt.api;
    requires okio;
    requires java.mail;
    requires jdk.httpserver;
    requires client.sdk;
<<<<<<< HEAD
    requires com.fasterxml.jackson.databind;
    requires java.net.http;    // Ouvre les packages pour FXML et JavaFX
=======
    requires de.jensd.fx.glyphs.fontawesome;

    // Ouvre les packages pour FXML et JavaFX
>>>>>>> c4b2604c53af71676c8e2a05719f0672e71995d9
    opens com.pfe.nova to javafx.fxml;
    opens com.pfe.nova.Application to javafx.graphics, javafx.fxml;
    opens com.pfe.nova.models to javafx.base, javafx.fxml,com.google.api.client;
    opens com.pfe.nova.Controller to javafx.fxml;
    opens com.pfe.nova.configuration to javafx.fxml;
    opens com.pfe.nova.services to javafx.fxml;

    // Exporte les packages (facultatif si besoin externe)
    exports com.pfe.nova;
    exports com.pfe.nova.Application;
    exports com.pfe.nova.Controller;
    exports com.pfe.nova.models;
    exports com.pfe.nova.services;

}
