module com.pfe.nova {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.sql;
    requires jbcrypt;

    opens com.pfe.nova to javafx.fxml;
    opens com.pfe.nova.Application to javafx.graphics;
    opens com.pfe.nova.models to javafx.fxml;
    opens com.pfe.nova.Controller to javafx.fxml;

    exports com.pfe.nova;
    exports com.pfe.nova.Application;
    exports com.pfe.nova.Controller;
    exports com.pfe.nova.models;
}