module com.pfe.nova {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.sql;
    requires jbcrypt;
    requires jakarta.mail;
    requires java.net.http;
    requires jdk.httpserver;  
    
    requires com.google.api.client;
    requires google.api.services.oauth2.v2.rev157;
    requires java.desktop;
    requires com.google.api.client.json.gson;
    requires google.api.client;


    opens com.pfe.nova to javafx.fxml;
    opens com.pfe.nova.Application to javafx.graphics;
    opens com.pfe.nova.models to javafx.fxml, com.google.api.client;
    opens com.pfe.nova.Controller to javafx.fxml;

    exports com.pfe.nova;
    exports com.pfe.nova.Application;
    exports com.pfe.nova.Controller;
    exports com.pfe.nova.models;
}