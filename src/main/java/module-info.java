module com.pfe.nova {
    // JavaFX
    requires javafx.fxml;
    requires itextpdf;
    requires org.controlsfx.controls;
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

    requires java.mail;
    requires jdk.httpserver;
    requires client.sdk;
    requires javafx.web;
    requires jdk.jsobject;
    requires okhttp3;
    requires de.jensd.fx.glyphs.fontawesome;
    requires jjwt.api;
    requires okio;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires java.desktop;
    requires java.net.http;    // Ouvre les packages pour FXML et JavaFX
    opens com.pfe.nova to javafx.fxml;
    opens com.pfe.nova.Application to javafx.graphics, javafx.fxml;
    opens com.pfe.nova.models to javafx.base, javafx.fxml,com.google.api.client;
    opens com.pfe.nova.Controller to javafx.fxml;
    opens com.pfe.nova.configuration to javafx.fxml;

    // Exporte les packages (facultatif si besoin externe)
    exports com.pfe.nova;
    exports com.pfe.nova.Application;
    exports com.pfe.nova.Controller;
    exports com.pfe.nova.models;
}
