module com.pfe.nova {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jbcrypt;

    opens com.pfe.nova to javafx.fxml;
    opens com.pfe.nova.Application to javafx.graphics;
    opens com.pfe.nova.configuration to javafx.fxml;
    opens com.pfe.nova.models to javafx.fxml;
    exports com.pfe.nova;
    exports com.pfe.nova.Application;
    exports com.pfe.nova.Controller;
    opens com.pfe.nova.Controller to javafx.fxml;
    exports com.pfe.nova.services;
    opens com.pfe.nova.services to javafx.fxml;

}