package com.pfe.nova.Controller;

import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.*;
import com.pfe.nova.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;
    @FXML private VBox roleSpecificContent;
    
    @FXML private Tab patientsTab;
    @FXML private Tab findDoctorsTab;
    @FXML private Tab adminTab;  // Add this FXML injection at the top with other tab declarations
    @FXML private Tab appointmentsTab;
    @FXML private Tab donationsTab;
    
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> patientNameColumn;
    @FXML private TableColumn<Patient, String> patientEmailColumn;
    @FXML private TableColumn<Patient, Integer> patientAgeColumn;
    @FXML private TableColumn<Patient, String> patientGenderColumn;
    @FXML private TableColumn<Patient, String> patientBloodTypeColumn;
    
    @FXML private TableView<Medecin> doctorsTable;
    @FXML private TableColumn<Medecin, String> doctorNameColumn;
    @FXML private TableColumn<Medecin, String> specialityColumn;
    @FXML private TableColumn<Medecin, String> experienceColumn;
    @FXML private TableColumn<Medecin, String> contactColumn;
    
    private User currentUser;

    @FXML
    public void initialize() {
        try {
            setupTableColumns();
            // Make sure all FXML elements are properly injected
            if (welcomeLabel == null || nameLabel == null || emailLabel == null || 
                phoneLabel == null || addressLabel == null || roleSpecificContent == null) {
                throw new RuntimeException("Failed to inject FXML components");
            }
        } catch (Exception e) {
            System.err.println("Error in DashboardController initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        // Setup Patient table columns
        patientNameColumn.setCellValueFactory(data -> 
            javafx.beans.binding.Bindings.concat(data.getValue().getNom(), " ", data.getValue().getPrenom()));
        patientEmailColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        patientAgeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getAge()));
        patientGenderColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getGender()));
        patientBloodTypeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBloodType()));

        // Setup Doctor table columns
        doctorNameColumn.setCellValueFactory(data -> 
            javafx.beans.binding.Bindings.concat(data.getValue().getNom(), " ", data.getValue().getPrenom()));
        specialityColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getSpecialite()));
        experienceColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getExperience()));
        contactColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getTel()));
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
//        try {
//            Parent root = FXMLLoader.load(getClass().getResource("/com/pfe/novaview/create-rapport.fxml"));
//            Stage stage = new Stage();
//            stage.setTitle("Create Rapport");
//            stage.setScene(new Scene(root));
//            stage.show();
//        } catch (IOException e) {
//            showError("Error loading Create Rapport page: " + e.getMessage());
//            e.printStackTrace();
//        }
    }

    @FXML
    private void handleViewRapports() {
//        try {
//            Parent root = FXMLLoader.load(getClass().getResource("/com/pfe/novaview/view-rapports.fxml"));
//            Stage stage = new Stage();
//            stage.setTitle("View Rapports");
//            stage.setScene(new Scene(root));
//            stage.show();
//        } catch (IOException e) {
//            showError("Error loading View Rapports page: " + e.getMessage());
//            e.printStackTrace();
//        }
    }


    @FXML private Label sessionTestLabel;

    private void setupUserInterface() {
        welcomeLabel.setText("Welcome, " + currentUser.getNom() + " " + currentUser.getPrenom());
        nameLabel.setText("Name: " + currentUser.getNom() + " " + currentUser.getPrenom());
        emailLabel.setText("Email: " + currentUser.getEmail());
        phoneLabel.setText("Phone: " + currentUser.getTel());
        addressLabel.setText("Address: " + currentUser.getAdresse());

        // Test session and display connected user
        User sessionUser = Session.getUtilisateurConnecte();
        if (sessionUser != null) {
            sessionTestLabel.setText("Session User: " + sessionUser.getEmail());
        } else {
            sessionTestLabel.setText("No user in session.");
        }

        setupRoleSpecificContent();
    }
//    private void setupUserInterface() {
//        welcomeLabel.setText("Welcome, " + currentUser.getNom() + " " + currentUser.getPrenom());
//        nameLabel.setText("Name: " + currentUser.getNom() + " " + currentUser.getPrenom());
//        emailLabel.setText("Email: " + currentUser.getEmail());
//        phoneLabel.setText("Phone: " + currentUser.getTel());
//        addressLabel.setText("Address: " + currentUser.getAdresse());
//
//        // Show/hide tabs based on user role
//        if (patientsTab != null) patientsTab.setDisable(!(currentUser instanceof Medecin));
//        if (findDoctorsTab != null) findDoctorsTab.setDisable(!(currentUser instanceof Patient));
//        if (adminTab != null) adminTab.setDisable(!(currentUser.getRole().equals("ADMIN")));
//        if (donationsTab != null) donationsTab.setDisable(!(currentUser instanceof Donateur));
//
//        setupRoleSpecificContent();
//    }

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
            patientsTable.setItems(loadPatients());
        } else if (currentUser instanceof Patient) {
            doctorsTable.setItems(loadMedecins());
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
            Parent root = FXMLLoader.load(getClass().getResource("/com/pfe/novaview/login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error during logout: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}