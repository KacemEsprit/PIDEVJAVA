package com.pfe.nova.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label statusLabel;
    @FXML
    private Label welcomeLabel;

    private String username;

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

    @FXML
    private void handleButton1() {
        statusLabel.setText("Button 1 clicked");
    }

    @FXML
    private void handleButton2() {
        statusLabel.setText("Button 2 clicked");
    }

    @FXML
    private void handleButton3() {
        statusLabel.setText("Button 3 clicked");
    }
}