package com.pfe.nova.Application;

import com.pfe.nova.configuration.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
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
    }
    public static void main(String[] args) {
        launch(args);
    }
}