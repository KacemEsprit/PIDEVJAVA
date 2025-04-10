package com.pfe.nova.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Label statusLabel;
    @FXML
    private Label welcomeLabel;

    private String username;
    private int connectedDoctorId; // Define the connectedDoctorId field

    @FXML
    public void initialize() {
        if (username != null) {
            welcomeLabel.setText("Welcome " + username);
        }
    }

    public void setUsername(String username) {
        this.username = username;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome " + username);
        }
    }

    public void setConnectedDoctorId(int connectedDoctorId) {
        this.connectedDoctorId = connectedDoctorId; // Setter for connectedDoctorId
    }

    @FXML
    private void handleButton1() {
        statusLabel.setText("Button 1 clicked");
    }

    @FXML
    private void handleButton2() {
        statusLabel.setText("Button 2 clicked");
    }

    @FXML
    private void handleButton3(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/rapport_form.fxml"));
            Parent root = loader.load();
            RapportController controller = loader.getController();
            controller.setConnectedMedecinId(connectedDoctorId); // Use the connectedDoctorId

            // Get the current stage from the event source
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            stage.setScene(new Scene(root));
            stage.setTitle("Rapport Form");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}