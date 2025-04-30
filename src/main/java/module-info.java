module com.pfe.nova {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.sql;
    requires jbcrypt;
    requires org.json;
    requires java.desktop;
    requires de.jensd.fx.glyphs.fontawesome;
    requires okhttp3;
    requires java.net.http;
    requires jjwt.api;
    requires okio;
    // Remplacer requires java.mail par requires jakarta.mail
    requires jakarta.mail;
    
    opens com.pfe.nova to javafx.fxml, de.jensd.fx.glyphs.fontawesome;
    opens com.pfe.nova.Application to javafx.graphics;
    opens com.pfe.nova.models to javafx.fxml;
    opens com.pfe.nova.Controller to javafx.fxml;
    opens com.pfe.nova.services to javafx.fxml;

    exports com.pfe.nova;
    exports com.pfe.nova.Application;
    exports com.pfe.nova.Controller;
    exports com.pfe.nova.models;
    exports com.pfe.nova.services;
}