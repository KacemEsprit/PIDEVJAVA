package com.pfe.nova.Controller;

import com.pfe.nova.configuration.AppointmentDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class RapportAppointmentController implements Initializable {

    @FXML private TextArea rapportTextArea;

    private int appointmentId;
    private AppointmentDAO appointmentDAO;
    private boolean isEditMode;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        appointmentDAO = new AppointmentDAO();
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
        // Check if a rapport already exists
        String existingRapport = appointmentDAO.getRapportByRendezvousId(appointmentId);
        if (existingRapport != null) {
            isEditMode = true;
            rapportTextArea.setText(existingRapport);
        } else {
            isEditMode = false;
        }
    }

    @FXML
    private void handleSaveRapport() {
        String rapportText = rapportTextArea.getText();
        if (!rapportText.isEmpty()) {
            boolean success;
            if (isEditMode) {
                success = appointmentDAO.updateRapport(appointmentId, rapportText);
            } else {
                success = appointmentDAO.saveRapport(appointmentId, rapportText);
            }
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", isEditMode ? "Rapport updated successfully." : "Rapport saved successfully.");
                Stage stage = (Stage) rapportTextArea.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", isEditMode ? "Failed to update rapport." : "Failed to save rapport.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Rapport content cannot be empty.");
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) rapportTextArea.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}