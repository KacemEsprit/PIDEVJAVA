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
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class CreateAppointmentController implements Initializable {

    @FXML private ComboBox<User> doctorComboBox;
    @FXML private DatePicker appointmentDate;
    @FXML private ComboBox<String> appointmentTime;

    private final UserDAO userDAO = new UserDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load doctors into ComboBox
        List<User> doctors = userDAO.getAllDoctors();
        doctorComboBox.setItems(FXCollections.observableArrayList(doctors));

        // Initialize time slots
        ObservableList<String> timeSlots = FXCollections.observableArrayList(
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"
        );
        appointmentTime.setItems(timeSlots);

        // Disable past dates in the DatePicker
        appointmentDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    @FXML
    private void handleSaveAppointment() {
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
                showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment created successfully.");
                clearForm(); // Clear the form after saving
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create appointment.");
            }
        }
    }

    @FXML
    private void handleCancel() {
        clearForm(); // Clear the form when canceling
    }

    private boolean validateInput() {
        if (doctorComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a doctor.");
            return false;
        }
        if (appointmentDate.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a date.");
            return false;
        }
        if (appointmentTime.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a time.");
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

    public void setCurrentUser(User currentUser) {
        // This method can be used to set the current user if needed
        // For example, you might want to display the user's name in the UI
        // or use it for other purposes.
        Session.setCurrentUser(currentUser);
    }
}