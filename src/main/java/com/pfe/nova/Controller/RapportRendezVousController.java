package com.pfe.nova.Controller;

import com.pfe.nova.configuration.AppointmentDAO;
import com.pfe.nova.models.Appointment;
import com.pfe.nova.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RapportRendezVousController implements Initializable {

    @FXML private FlowPane appointmentsCardContainer;

    private AppointmentDAO appointmentDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        appointmentDAO = new AppointmentDAO();
        loadAppointmentsForMedecin();
    }

    private void loadAppointmentsForMedecin() {
        int doctorId = Session.getCurrentUser().getId();
        List<Appointment> doctorAppointments = appointmentDAO.getDoctorAppointments(doctorId);

        // Clear existing cards
        appointmentsCardContainer.getChildren().clear();

        // Create a card for each appointment
        for (Appointment appointment : doctorAppointments) {
            VBox card = createAppointmentCard(appointment);
            appointmentsCardContainer.getChildren().add(card);
        }
    }

    private VBox createAppointmentCard(Appointment appointment) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label dateLabel = new Label("Date: " + appointment.getAppointmentDateTime().toLocalDate());
        Label timeLabel = new Label("Time: " + appointment.getAppointmentDateTime().toLocalTime());
        Label patientLabel = new Label("Patient: " + appointmentDAO.getPatientNameById(appointment.getPatientId()));
        Label statusLabel = new Label("Status: " + appointment.getStatus());

        HBox actionsBox = new HBox(10);

        // Check if a rapport exists for this appointment
        boolean hasRapport = appointmentDAO.getRapportByRendezvousId(appointment.getId()) != null;
        Button rapportButton = new Button(hasRapport ? "Edit Rapport" : "Create Rapport");
        rapportButton.setStyle(hasRapport ? "-fx-background-color: #f39c12; -fx-text-fill: white;" : "-fx-background-color: #27ae60; -fx-text-fill: white;");
        rapportButton.setOnAction(e -> handleCreateRapport(appointment));

        Button viewRapportButton = new Button("View Rapport");
        viewRapportButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        viewRapportButton.setOnAction(e -> handleViewRapport(appointment));

        Button deleteRapportButton = null;
        if (hasRapport) {
            deleteRapportButton = new Button("Delete Rapport");
            deleteRapportButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            deleteRapportButton.setOnAction(e -> handleDeleteRapport(appointment));
        }

        actionsBox.getChildren().addAll(rapportButton, viewRapportButton);
        if (deleteRapportButton != null) {
            actionsBox.getChildren().add(deleteRapportButton);
        }

        card.getChildren().addAll(dateLabel, timeLabel, patientLabel, statusLabel, actionsBox);
        return card;
    }

    private void handleCreateRapport(Appointment appointment) {
        try {
            String resourcePath = "/com/pfe/novaview/rapport_appointment_dialog.fxml";
            URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl == null) {
                resourceUrl = ClassLoader.getSystemClassLoader().getResource("com/pfe/novaview/rapport_appointment_dialog.fxml");
                if (resourceUrl == null) {
                    showError("Rapport dialog FXML resource not found at " + resourcePath);
                    Logger.getLogger(RapportRendezVousController.class.getName()).log(Level.SEVERE,
                            "Rapport dialog FXML resource not found at {0}. Classpath: {1}",
                            new Object[]{resourcePath, System.getProperty("java.class.path")});
                    return;
                }
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            RapportAppointmentController controller = loader.getController();
            if (controller != null) {
                controller.setAppointmentId(appointment.getId());
            }
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(hasRapport(appointment.getId()) ? "Edit Rapport for Appointment" : "Create Rapport for Appointment");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            // Refresh the appointments list after creating or editing a rapport
            loadAppointmentsForMedecin();
        } catch (Exception e) {
            showError("Failed to open rapport dialog: " + e.getMessage());
            Logger.getLogger(RapportRendezVousController.class.getName()).log(Level.SEVERE,
                    "Failed to load rapport dialog", e);
        }
    }

    private boolean hasRapport(int appointmentId) {
        return appointmentDAO.getRapportByRendezvousId(appointmentId) != null;
    }

    private void handleViewRapport(Appointment appointment) {
        String rapportContent = appointmentDAO.getRapportByRendezvousId(appointment.getId());
        if (rapportContent != null) {
            // Create a dialog to display the rapport
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("View Rapport for Appointment ID: " + appointment.getId());

            VBox dialogVBox = new VBox(10);
            dialogVBox.setStyle("-fx-padding: 20;");

            Label titleLabel = new Label("Rapport Content");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            TextArea rapportTextArea = new TextArea(rapportContent);
            rapportTextArea.setPrefHeight(200);
            rapportTextArea.setPrefWidth(300);
            rapportTextArea.setWrapText(true);
            rapportTextArea.setEditable(false);

            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            closeButton.setOnAction(e -> stage.close());

            dialogVBox.getChildren().addAll(titleLabel, rapportTextArea, closeButton);

            Scene dialogScene = new Scene(dialogVBox);
            stage.setScene(dialogScene);
            stage.showAndWait();
        } else {
            showError("No rapport found for appointment ID: " + appointment.getId());
        }
    }

    private void handleDeleteRapport(Appointment appointment) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete the rapport for appointment ID: " + appointment.getId() + "?");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = appointmentDAO.deleteRapport(appointment.getId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Rapport deleted successfully.");
                    // Refresh the appointments list after deletion
                    loadAppointmentsForMedecin();
                } else {
                    showError("Failed to delete rapport.");
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}