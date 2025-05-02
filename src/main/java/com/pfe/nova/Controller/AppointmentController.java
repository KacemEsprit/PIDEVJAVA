package com.pfe.nova.Controller;

import com.pfe.nova.configuration.AppointmentDAO;
import com.pfe.nova.configuration.UserDAO;
import com.pfe.nova.models.Appointment;
import com.pfe.nova.models.User;
import com.pfe.nova.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class AppointmentController implements Initializable {
    @FXML
    private ComboBox<User> doctorComboBox;
    @FXML
    private DatePicker appointmentDate;
    @FXML
    private ComboBox<String> appointmentTime;
    @FXML
    private FlowPane appointmentsCardContainer;
    @FXML
    private GridPane appointmentsGrid;
    @FXML
    private HBox adzadza;
    @FXML
    private Label aa;

    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        appointmentDAO = new AppointmentDAO();
        userDAO = new UserDAO();

        // Check if the user is a Medecin
        if ("MEDECIN".equals(Session.getCurrentUserRole())) {
            // Hide the "Schedule New Appointment" section
            appointmentsGrid.setVisible(false);
            appointmentsGrid.setManaged(false); // Prevent occupying space
            aa.setVisible(false);
            aa.setManaged(false); // Prevent occupying space
            adzadza.setVisible(false);
            adzadza.setManaged(false); // Prevent occupying space

            // Load appointments for the Medecin
            loadAppointmentsForMedecin();
        } else {
            // Initialize for non-Medecin users (e.g., Patient)
            ObservableList<String> timeSlots = FXCollections.observableArrayList(
                    "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                    "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"
            );
            appointmentTime.setItems(timeSlots);

            List<User> doctors = userDAO.getAllDoctors();
            doctorComboBox.setItems(FXCollections.observableArrayList(doctors));

            loadAppointmentsForPatient();
        }
    }

    private void loadAppointmentsForMedecin() {
        int doctorId = Session.getCurrentUser().getId();
        List<Appointment> doctorAppointments = appointmentDAO.getDoctorAppointments(doctorId);

        // Clear existing cards
        appointmentsCardContainer.getChildren().clear();

        // Create a card for each appointment
        for (Appointment appointment : doctorAppointments) {
            VBox card = createAppointmentCardForMedecin(appointment);
            appointmentsCardContainer.getChildren().add(card);
        }
    }

    private void loadAppointmentsForPatient() {
        int patientId = Session.getCurrentUser().getId();
        List<Appointment> patientAppointments = appointmentDAO.getPatientAppointments(patientId);

        // Clear existing cards
        appointmentsCardContainer.getChildren().clear();

        // Create a card for each appointment
        for (Appointment appointment : patientAppointments) {
            VBox card = createAppointmentCardForPatient(appointment);
            appointmentsCardContainer.getChildren().add(card);
        }
    }

    private VBox createAppointmentCardForMedecin(Appointment appointment) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label dateLabel = new Label("Date: " + appointment.getAppointmentDateTime().toLocalDate());
        Label timeLabel = new Label("Time: " + appointment.getAppointmentDateTime().toLocalTime());
        Label patientLabel = new Label("Patient: " + userDAO.getUserById(appointment.getPatientId()));
        Label statusLabel = new Label("Status: " + appointment.getStatus());

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> handleCancelAppointment(appointment));

        card.getChildren().addAll(dateLabel, timeLabel, patientLabel, statusLabel, cancelButton);
        return card;
    }

    private VBox createAppointmentCardForPatient(Appointment appointment) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label dateLabel = new Label("Date: " + appointment.getAppointmentDateTime().toLocalDate());
        Label timeLabel = new Label("Time: " + appointment.getAppointmentDateTime().toLocalTime());
        Label doctorLabel = new Label("Doctor: " + userDAO.getUserById(appointment.getDoctorId()));
        Label statusLabel = new Label("Status: " + appointment.getStatus());

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> handleCancelAppointment(appointment));

        card.getChildren().addAll(dateLabel, timeLabel, doctorLabel, statusLabel, cancelButton);
        return card;
    }

    @FXML
    private void handleCreateAppointment() {
        if (validateInput()) {
            User selectedDoctor = doctorComboBox.getValue();
            LocalDate date = appointmentDate.getValue();
            LocalTime time = LocalTime.parse(appointmentTime.getValue());
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            Appointment appointment = new Appointment();
            appointment.setPatientId(Session.getCurrentUser().getId());
            appointment.setDoctorId(selectedDoctor.getId());
            appointment.setAppointmentDateTime(dateTime);
            appointment.setStatus("SCHEDULED");

            if (appointmentDAO.createAppointment(appointment)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment created successfully");
                loadAppointmentsForPatient();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create appointment");
            }
        }
    }

    private void handleCancelAppointment(Appointment appointment) {
        if (appointment != null && "SCHEDULED".equals(appointment.getStatus())) {
            if (appointmentDAO.cancelAppointment(appointment.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment cancelled successfully");
                if ("MEDECIN".equals(Session.getCurrentUserRole())) {
                    loadAppointmentsForMedecin();
                } else {
                    loadAppointmentsForPatient();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel appointment");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Cannot cancel this appointment");
        }
    }

    @FXML
    private void handleCancelAppointment() {
        // Not used since top-level cancel button is removed
        showAlert(Alert.AlertType.WARNING, "Warning", "Please select an appointment to cancel");
    }

    private boolean validateInput() {
        if (doctorComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a doctor");
            return false;
        }
        if (appointmentDate.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a date");
            return false;
        }
        if (appointmentTime.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a time");
            return false;
        }
        return true;
    }

    private void clearForm() {
        doctorComboBox.setValue(null);
        appointmentDate.setValue(null);
        appointmentTime.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}