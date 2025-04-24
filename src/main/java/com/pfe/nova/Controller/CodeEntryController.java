package com.pfe.nova.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CodeEntryController {
    @FXML private TextField codeField;
    @FXML private Button verifyButton;
    @FXML private Label infoLabel;

    private String email; // Set this when opening the form

    public void setEmail(String email) {
        this.email = email;
        System.out.println("Email set in CodeEntryController: " + email);
    }

    @FXML
    private void handleVerify() {
        String code = codeField.getText().trim(); // Add trim to remove any whitespace
        System.out.println("Attempting to verify code: " + code + " for email: " + email);
        
        if (ForgotPasswordController.verifyResetCode(email, code)) {
            infoLabel.setText("Code verified! Proceed to reset password.");
            infoLabel.setVisible(true);
            openResetPasswordForm(email);
        } else {
            infoLabel.setText("Invalid code. Please try again.");
            infoLabel.setVisible(true);
        }
    }

    private void openResetPasswordForm(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/reset-password.fxml"));
            Parent root = loader.load();
            ResetPasswordController controller = loader.getController();
            controller.setEmail(email);
            Stage stage = (Stage) verifyButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Reset Password");
            stage.centerOnScreen();
        } catch (Exception e) {
            infoLabel.setText("Unable to open reset password form: " + e.getMessage());
            infoLabel.setVisible(true);
            e.printStackTrace();
        }
    }
}