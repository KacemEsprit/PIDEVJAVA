package com.pfe.nova.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class GoogleAuthCodeController {
    @FXML private TextField codeField;
    @FXML private Button verifyButton;
    @FXML private Label infoLabel;
    
    private Consumer<String> callback;
    
    public void setCallback(Consumer<String> callback) {
        this.callback = callback;
    }
    
    @FXML
    private void handleVerify() {
        String code = codeField.getText().trim();
        
        if (code.isEmpty()) {
            showError("Please enter the Google authentication code");
            return;
        }
        
        // Close the dialog
        Stage stage = (Stage) verifyButton.getScene().getWindow();
        stage.close();
        
        // Call the callback with the code
        if (callback != null) {
            callback.accept(code);
        }
    }
    
    private void showError(String message) {
        infoLabel.setText(message);
        infoLabel.setVisible(true);
    }
}