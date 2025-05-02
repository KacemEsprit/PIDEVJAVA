package com.pfe.nova.Controller;

import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.configuration.AppointmentDAO;
import com.pfe.nova.models.*;
import com.pfe.nova.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Optional;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.geometry.Insets;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;
    @FXML private VBox roleSpecificContent;
    @FXML private StackPane contentArea;

    @FXML private TabPane patientsTab;  // Matches fx:id="patientsTab" in dashboard.fxml
    @FXML private Tab patientsTabItem;  // Matches fx:id="patientsTabItem"
    @FXML private Tab findDoctorsTabItem;  // Matches fx:id="findDoctorsTabItem"
    @FXML private Tab adminTabItem;  // Matches fx:id="adminTabItem"
    @FXML private Tab appointmentsTabItem;  // Matches fx:id="appointmentsTabItem"
    @FXML private Tab donationsTabItem;  // Matches fx:id="donationsTabItem"
    @FXML private Tab createRapportTabItem;  // Matches fx:id="createRapportTabItem"
    @FXML private Tab viewRapportTabItem;  // Matches fx:id="viewRapportTabItem"

    @FXML private VBox patientsContainer;
    @FXML private VBox doctorsContainer;
    @FXML private VBox appointmentsContainer;

    @FXML private Button rapportRendezVousButton;  // Matches fx:id="rapportRendezVousButton"

    private User currentUser;
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            // Get current user
            this.currentUser = Session.getUtilisateurConnecte();
            boolean isMedecin = currentUser instanceof Medecin;

            // Enable/disable rapport tabs based on user type
            if (createRapportTabItem != null) {
                createRapportTabItem.setDisable(!isMedecin);
            }
            if (viewRapportTabItem != null) {
                viewRapportTabItem.setDisable(!isMedecin);
            }

            // Enable appointments tab
            if (appointmentsTabItem != null) {
                appointmentsTabItem.setDisable(false);
            }

            // Show Rapport RendezVous button only for Medecin
            if (rapportRendezVousButton != null) {
                boolean isVisible = isMedecin;
                rapportRendezVousButton.setVisible(isVisible);
                rapportRendezVousButton.setManaged(isVisible);
            }

            // Make sure all FXML elements are properly injected
            if (welcomeLabel == null || nameLabel == null || emailLabel == null ||
                    phoneLabel == null || addressLabel == null || roleSpecificContent == null ||
                    contentArea == null) {
                showError("Failed to inject FXML components");
                return;
            }

            // Setup card-based views
            setupCardBasedViews();
        } catch (Exception e) {
            showError("Error in DashboardController initialization: " + e.getMessage());
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void setupCardBasedViews() {
        // Setup card-based views for appointments
        if (appointmentsContainer != null) {
            appointmentsContainer.getChildren().clear();
            appointmentsContainer.setSpacing(10);
            appointmentsContainer.setPadding(new Insets(10));

            // Load appointments and create cards
            for (Appointment appointment : appointments) {
                VBox card = createAppointmentCard(appointment);
                appointmentsContainer.getChildren().add(card);
            }
        }
    }

    private VBox createAppointmentCard(Appointment appointment) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label dateLabel = new Label("Date: " + appointment.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Label timeLabel = new Label("Time: " + appointment.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        String withLabel;
        if (currentUser instanceof Medecin) {
            AppointmentDAO appointmentDAO = new AppointmentDAO();
            withLabel = "Patient: " + appointmentDAO.getPatientNameById(appointment.getPatientId());
        } else {
            withLabel = "Doctor: " + getDoctorNameById(appointment.getDoctorId());
        }
        Label withPersonLabel = new Label(withLabel);
        Label statusLabel = new Label("Status: " + appointment.getStatus());

        HBox actionsBox = new HBox(10);
        if (currentUser instanceof Medecin) {
            Button editButton = new Button("Edit");
            Button cancelButton = new Button("Cancel");
            Button rapportButton = new Button("Create Rapport");

            editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
            cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            rapportButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

            editButton.setOnAction(e -> showEditAppointmentDialog(appointment));
            cancelButton.setOnAction(e -> handleCancelAppointment(appointment));
            rapportButton.setOnAction(e -> showRapportDialog(appointment));

            actionsBox.getChildren().addAll(editButton, cancelButton, rapportButton);
        }

        card.getChildren().addAll(dateLabel, timeLabel, withPersonLabel, statusLabel, actionsBox);
        return card;
    }

    private void showEditAppointmentDialog(Appointment appointment) {
        Dialog<LocalDateTime> dialog = new Dialog<>();
        dialog.setTitle("Edit Appointment");
        dialog.setHeaderText("Change appointment date and time");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(appointment.getAppointmentDateTime().toLocalDate());
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, appointment.getAppointmentDateTime().getHour());
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, appointment.getAppointmentDateTime().getMinute());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Time:"), 0, 1);

        HBox timeBox = new HBox(5);
        timeBox.getChildren().addAll(hourSpinner, new Label(":"), minuteSpinner);
        grid.add(timeBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return LocalDateTime.of(
                        datePicker.getValue(),
                        LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())
                );
            }
            return null;
        });

        Optional<LocalDateTime> result = dialog.showAndWait();
        result.ifPresent(newDateTime -> {
            AppointmentDAO appointmentDAO = new AppointmentDAO();
            if (appointmentDAO.updateAppointmentDateTime(appointment.getId(), newDateTime)) {
                appointment.setAppointmentDateTime(newDateTime);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update appointment.");
            }
        });
    }

    private void showRapportDialog(Appointment appointment) {
        try {
            String resourcePath = "/com/pfe/novaview/rapport_appointment_dialog.fxml";
            URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl == null) {
                resourceUrl = ClassLoader.getSystemClassLoader().getResource("com/pfe/novaview/rapport_appointment_dialog.fxml");
                if (resourceUrl == null) {
                    showError("Rapport dialog FXML resource not found at " + resourcePath);
                    Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE,
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
            stage.setTitle("Create Rapport for Appointment");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            showError("Failed to open rapport dialog: " + e.getMessage());
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE,
                    "Failed to load rapport dialog", e);
        }
    }

    private void handleCancelAppointment(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Appointment");
        alert.setHeaderText("Are you sure you want to cancel this appointment?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            AppointmentDAO appointmentDAO = new AppointmentDAO();
            if (appointmentDAO.cancelAppointment(appointment.getId())) {
                appointment.setStatus("CANCELLED");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment cancelled successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel appointment.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void initData(User user) {
        this.currentUser = user;
        setupUserInterface();
        loadData();
    }

    @FXML private Button createRapportButton;
    @FXML private Button viewRapportsButton;

    @FXML
    private void handleCreateRapport() {
        // Placeholder for future implementation
    }

    @FXML
    private void handleViewRapports() {
        // Placeholder for future implementation
    }

    @FXML
    private void handleNewAppointment() {
        try {
            String resourcePath = "/com/pfe/nova/create_appointment.fxml";
            URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl == null) {
                resourceUrl = ClassLoader.getSystemClassLoader().getResource("com/pfe/nova/create_appointment.fxml");
                if (resourceUrl == null) {
                    showError("Create appointment FXML resource not found at " + resourcePath);
                    Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE,
                            "Create appointment FXML resource not found at {0}. Classpath: {1}",
                            new Object[]{resourcePath, System.getProperty("java.class.path")});
                    return;
                }
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Schedule New Appointment");
            stage.setScene(new Scene(root));

            CreateAppointmentController controller = loader.getController();
            if (controller != null) {
                controller.setCurrentUser(currentUser);
            }

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open appointment form: " + e.getMessage());
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE,
                    "Failed to load appointment form", e);
        }
    }

    @FXML private Label sessionTestLabel;

    private void setupUserInterface() {
        welcomeLabel.setText("Welcome, " + currentUser.getNom() + " " + currentUser.getPrenom());
        nameLabel.setText("Name: " + currentUser.getNom() + " " + currentUser.getPrenom());
        emailLabel.setText("Email: " + currentUser.getEmail());
        phoneLabel.setText("Phone: " + currentUser.getTel());
        addressLabel.setText("Address: " + currentUser.getAdresse());

        User sessionUser = Session.getUtilisateurConnecte();
        if (sessionUser != null) {
            sessionTestLabel.setText("Session User: " + sessionUser.getEmail());
        } else {
            sessionTestLabel.setText("No user in session.");
        }

        if (patientsTabItem != null) {
            patientsTabItem.setDisable(!(currentUser instanceof Medecin));
        }
        if (findDoctorsTabItem != null) {
            findDoctorsTabItem.setDisable(!(currentUser instanceof Patient));
        }
        if (adminTabItem != null) {
            adminTabItem.setDisable(!currentUser.getRole().equals("ADMIN"));
        }
        if (donationsTabItem != null) {
            donationsTabItem.setDisable(!(currentUser instanceof Donateur));
        }

        // Show Rapport RendezVous button only for Medecin
        if (rapportRendezVousButton != null) {
            boolean isVisible = currentUser instanceof Medecin;
            rapportRendezVousButton.setVisible(isVisible);
            rapportRendezVousButton.setManaged(isVisible);
        }

        setupRoleSpecificContent();
    }

    private void setupRoleSpecificContent() {
        roleSpecificContent.getChildren().clear();

        if (currentUser instanceof Medecin medecin) {
            roleSpecificContent.getChildren().addAll(
                    new Label("Speciality: " + medecin.getSpecialite()),
                    new Label("Experience: " + medecin.getExperience()),
                    new Label("Diploma: " + medecin.getDiplome())
            );
        } else if (currentUser instanceof Patient patient) {
            roleSpecificContent.getChildren().addAll(
                    new Label("Age: " + patient.getAge()),
                    new Label("Gender: " + patient.getGender()),
                    new Label("Blood Type: " + patient.getBloodType())
            );
        } else if (currentUser instanceof Donateur donateur) {
            roleSpecificContent.getChildren().add(
                    new Label("Donateur Type: " + donateur.getDonateurType())
            );
        }
    }

    private void loadData() {
        if (currentUser instanceof Medecin) {
            // patientsTable.setItems(loadPatients());
        } else if (currentUser instanceof Patient) {
            // doctorsTable.setItems(loadMedecins());
        }
    }

    private ObservableList<Patient> loadPatients() {
        ObservableList<Patient> patients = FXCollections.observableArrayList();
        String query = "SELECT * FROM user WHERE role = 'PATIENT'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                patients.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("tel"),
                        rs.getString("adresse"),
                        rs.getString("password"),
                        rs.getString("picture"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("blood_type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading patients: " + e.getMessage());
        }
        return patients;
    }

    private ObservableList<Medecin> loadMedecins() {
        ObservableList<Medecin> medecins = FXCollections.observableArrayList();
        String query = "SELECT * FROM user WHERE role = 'MEDECIN'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                medecins.add(new Medecin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("tel"),
                        rs.getString("adresse"),
                        rs.getString("password"),
                        rs.getString("picture"),
                        rs.getString("specialite"),
                        rs.getString("experience"),
                        rs.getString("diplome")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading doctors: " + e.getMessage());
        }
        return medecins;
    }

    @FXML
    private void handleLogout() {
        try {
            String resourcePath = "/com/pfe/novaview/login.fxml";
            URL resourceUrl = getClass().getResource(resourcePath);

            // Debug: Log the classpath and resource attempts
            Logger.getLogger(DashboardController.class.getName()).log(Level.INFO,
                    "Attempting to load login.fxml from: {0}", resourcePath);
            Logger.getLogger(DashboardController.class.getName()).log(Level.INFO,
                    "Classpath: {0}", System.getProperty("java.class.path"));

            // First attempt: getClass().getResource()
            if (resourceUrl == null) {
                Logger.getLogger(DashboardController.class.getName()).log(Level.WARNING,
                        "getClass().getResource failed for {0}, trying ClassLoader", resourcePath);
                // Second attempt: ClassLoader.getSystemClassLoader()
                resourceUrl = ClassLoader.getSystemClassLoader().getResource("com/pfe/nova/login.fxml");
                if (resourceUrl == null) {
                    // Third attempt: Use DashboardController.class.getClassLoader()
                    resourceUrl = DashboardController.class.getClassLoader().getResource("com/pfe/nova/login.fxml");
                    if (resourceUrl == null) {
                        showError("Login FXML resource not found at " + resourcePath);
                        Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE,
                                "All attempts to load login.fxml failed. Ensure login.fxml is in src/main/resources/com/pfe/nova/");
                        // Fallback: Close the window instead of leaving the user stuck
                        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                        stage.close();
                        return;
                    }
                }
            }

            Logger.getLogger(DashboardController.class.getName()).log(Level.INFO,
                    "Successfully found login.fxml at: {0}", resourceUrl);

            // Load the FXML and switch the scene
            Parent root = FXMLLoader.load(resourceUrl);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");

            // Clear the session
            Session.logout();
        } catch (Exception e) {
            showError("Error during logout: " + e.getMessage());
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE,
                    "Failed to load login view", e);
            // Fallback: Close the window if loading fails
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.close();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getDoctorNameById(int doctorId) {
        String name = "";
        String query = "SELECT nom, prenom FROM user WHERE id = ? AND role = 'MEDECIN'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("nom") + " " + rs.getString("prenom");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    @FXML
    private void handleRapportRendezVous() {
        try {
            String resourcePath = "/com/pfe/novaview/RapportRendezVous.fxml";
            URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl == null) {
                resourceUrl = ClassLoader.getSystemClassLoader().getResource("com/pfe/nova/RapportRendezVous.fxml");
                if (resourceUrl == null) {
                    showError("Rapport RendezVous FXML resource not found at " + resourcePath);
                    Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE,
                            "Rapport RendezVous FXML resource not found at {0}. Classpath: {1}",
                            new Object[]{resourcePath, System.getProperty("java.class.path")});
                    return;
                }
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Rapport RendezVous");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, e);
            showError("Unable to open Rapport RendezVous page: " + e.getMessage());
        }
    }

    @FXML
    private void handleAppointmentsNavigation() {
        try {
            String resourcePath = "/com/pfe/nova/appointment-view.fxml";
            URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl == null) {
                resourceUrl = ClassLoader.getSystemClassLoader().getResource("com/pfe/nova/appointment-view.fxml");
                if (resourceUrl == null) {
                    showError("Appointment view FXML resource not found at " + resourcePath);
                    Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE,
                            "Appointment view FXML resource not found at {0}. Classpath: {1}",
                            new Object[]{resourcePath, System.getProperty("java.class.path")});
                    return;
                }
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent appointmentView = loader.load();
            contentArea.getChildren().setAll(appointmentView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the Appointments view: " + e.getMessage());
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE,
                    "Failed to load Appointments view", e);
        }
    }

    @FXML
    private void handleChatNavigation() {
        try {
            String resourcePath = "/com/pfe/novaview/chat.fxml";
            URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl == null) {
                resourceUrl = ClassLoader.getSystemClassLoader().getResource("com/pfe/novaview/chat.fxml");
                if (resourceUrl == null) {
                    showError("Chat FXML resource not found at " + resourcePath);
                    Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE,
                            "Chat FXML resource not found at {0}. Classpath: {1}",
                            new Object[]{resourcePath, System.getProperty("java.class.path")});
                    return;
                }
            }
            Logger.getLogger(DashboardController.class.getName()).log(Level.INFO,
                    "Loading chat.fxml from: {0}", resourceUrl);
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent chatView = loader.load();
            contentArea.getChildren().setAll(chatView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the Chat view: " + e.getMessage());
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE,
                    "Failed to load Chat view", e);
        }
    }

    public void handleViewCart(ActionEvent actionEvent) {
    }

    public void showProfile(ActionEvent actionEvent) {
    }

    public void navigateToPostList(ActionEvent actionEvent) {
    }

    public void handleOrder(ActionEvent actionEvent) {
    }
}