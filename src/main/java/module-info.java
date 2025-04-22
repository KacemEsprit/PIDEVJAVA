module com.pfe.nova {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires transitive javafx.graphics;
    requires java.sql;
    requires jbcrypt;

    opens com.pfe.nova to javafx.fxml;
    opens com.pfe.nova.Application to javafx.graphics;
    opens com.pfe.nova.models to javafx.fxml;
    opens com.pfe.nova.Controller to javafx.fxml;
    opens com.pfe.nova.Controller.Compagnie to javafx.fxml;

    exports com.pfe.nova;
    exports com.pfe.nova.Application;
    exports com.pfe.nova.Controller;
    exports com.pfe.nova.models;

    exports com.pfe.nova.Controller.Don;
    exports com.pfe.nova.Controller.DonController;

    opens com.pfe.nova.Controller.Don to javafx.fxml;
    opens com.pfe.nova.Controller.DonController to javafx.fxml;
}