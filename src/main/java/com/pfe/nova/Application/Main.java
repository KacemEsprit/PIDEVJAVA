package com.pfe.nova.Application;

import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.Rapport;
import com.pfe.nova.services.RapportService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Test database connection
        testDatabaseConnection();

        // Using the verified correct path
        Parent root = FXMLLoader.load(getClass().getResource("/com/pfe/novaview/login.fxml"));

        primaryStage.setTitle("Login System");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }
    private void testDatabaseConnection() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (connection != null) {
                System.out.println("Connected to the database!");
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
        RapportService rapportService = new RapportService();

        // Create a sample Rapport object
        Rapport rapport = new Rapport();
        rapport.setPatientId(1);
        rapport.setMedecinId(2);
        rapport.setDateRapport(LocalDate.now());
        rapport.setSexe("Male");
        rapport.setAge(30);
        rapport.setTraitement("Treatment A");
        rapport.setDoseMedicament("50mg");
        rapport.setFrequenceTraitement("Twice a day");
        rapport.setPerteDeSang("Low");
        rapport.setTempsOperation("2 hours");
        rapport.setFiltrationSang("Normal");
        rapport.setCreatinine("1.2");
        rapport.setScoreGlasgow("15");
        rapport.setRespirationAssistee("No");
        rapport.setComplications("None");

        // Call the createRapport method
        boolean isCreated = rapportService.createRapport(rapport);

        // Print the result
        if (isCreated) {
            System.out.println("Rapport created successfully!");
        } else {
            System.out.println("Failed to create rapport.");
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}