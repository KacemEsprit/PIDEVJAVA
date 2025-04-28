package com.pfe.nova.Application;

import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.Controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        testDatabaseConnection();

        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/login.fxml"));
        Parent root = loader.load();
        
        // Get the controller and pass HostServices
        LoginController controller = loader.getController();
        controller.setHostServices(getHostServices());
        
        // Set up the stage
        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root, 1200, 800));
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
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}