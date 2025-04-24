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
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.nio.file.Paths;

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
    @FXML private ImageView profileImage; // Add this for profile image
    
    @FXML private TabPane contentTabPane; // Add this missing FXML field
    @FXML private Button adminPostsBtn; // Add this for the admin button
    
    @FXML private Tab patientsTab;
    @FXML private Tab findDoctorsTab;
    @FXML private Tab adminTab;
    @FXML private Tab appointmentsTab;
    @FXML private Tab donationsTab;
    @FXML private Tab communityPostsTab; // Add this new tab field
    
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> patientNameColumn;
    @FXML private TableColumn<Patient, String> patientEmailColumn;
    @FXML private TableColumn<Patient, Integer> patientAgeColumn;
    @FXML private TableColumn<Patient, String> patientGenderColumn;
    @FXML private TableColumn<Patient, String> patientBloodTypeColumn;
    
    @FXML private TableView<Medecin> doctorsTable;
    @FXML private TableColumn<Medecin, String> doctorNameColumn;
    @FXML private TableColumn<Medecin, String> doctorEmailColumn;
    @FXML private TableColumn<Medecin, String> doctorSpecialityColumn;
    @FXML private TableColumn<Medecin, String> doctorExperienceColumn;
    @FXML private TableColumn<Medecin, String> doctorDiplomeColumn;
    @FXML private TableColumn<Medecin, String> specialityColumn;
    @FXML private TableColumn<Medecin, String> experienceColumn;
    @FXML private TableColumn<Medecin, String> contactColumn;
    
    @FXML
    private Tab createRapportTab;
    
    @FXML
    private Tab viewRapportTab;

    private User currentUser;
    @FXML private Button communityPostsButton; // Add this field
    
    @FXML
    public void initialize() {
        try {
            setupTableColumns();
            // Get the current user from session
            User currentUser = Session.getInstance().getUtilisateurConnecte();
            
            if (currentUser == null) {
                System.err.println("No user found in session");
                return;
            }
            
            // Set visibility of tabs based on user role
            setupTabVisibility(currentUser);
            
            // Add tab selection listener
            contentTabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldTab, newTab) -> handleTabSelection(newTab)
            );
            
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

    private void handleTabSelection(Tab selectedTab) {
        if (selectedTab == null) return;
        
        // Handle tab selection based on tab ID
        if (selectedTab.equals(createRapportTab)) {
            handleCreateRapport();
        } else if (selectedTab.equals(viewRapportTab)) {
            handleViewRapports();
        } else if (selectedTab.equals(communityPostsTab)) {
            navigateToPostList();
        /* } else if (selectedTab.equals(appointmentsTab)) {
            handleAppointmentsTab();
        } else if (selectedTab.equals(donationsTab)) {
            handleDonationsTab();
        } else if (selectedTab.equals(adminTab)) {
            handleAdminTab(); */
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
        
        // Show/hide admin posts management button based on role
        if (adminPostsBtn != null) {
            boolean isAdmin = user.getRole() != null && user.getRole().toUpperCase().contains("ADMIN");
            adminPostsBtn.setVisible(isAdmin);
            adminPostsBtn.setManaged(isAdmin); // This removes the space when button is hidden
        }
        
        // Load profile image if available
        if (user.getPicture() != null && !user.getPicture().isEmpty()) {
            try {
                Image image = new Image(Paths.get(user.getPicture()).toUri().toString());
                if (profileImage != null) {
                    profileImage.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
            }
        }
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
        User sessionUser = Session.getInstance().getUtilisateurConnecte();
        if (sessionUser != null) {
            sessionTestLabel.setText("Session User: " + sessionUser.getEmail());
        } else {
            sessionTestLabel.setText("No user in session.");
        }

        setupRoleSpecificContent();
    }
    private void setupTabVisibility(User user) {
        if (user == null) return;
        
        // Check user instance type instead of role string
        boolean isMedecin = user instanceof Medecin;
        boolean isPatient = user instanceof Patient;
        boolean isDonateur = user instanceof Donateur;
        
        // Set visibility based on user type
        if (patientsTab != null) patientsTab.setDisable(!isMedecin);
        if (findDoctorsTab != null) findDoctorsTab.setDisable(!isPatient);
        
        // Admin will be redirected to adminDashboard, so we disable the admin tab for all users
        if (adminTab != null) adminTab.setDisable(true);
        
        // Donations tab is only for donateurs
        if (donationsTab != null) donationsTab.setDisable(!isDonateur);
        
        // Rapport tabs are only for doctors
        if (createRapportTab != null) createRapportTab.setDisable(!isMedecin);
        if (viewRapportTab != null) viewRapportTab.setDisable(!isMedecin);
        
        // Community posts tab is for patients
        if (communityPostsTab != null) {
            communityPostsTab.setDisable(!isPatient);
        }
        
        // Appointments tab is for both patients and doctors
        if (appointmentsTab != null) {
            appointmentsTab.setDisable(!(isPatient || isMedecin));
        }
        
        // Show/hide buttons based on role
        if (communityPostsButton != null) {
            communityPostsButton.setVisible(isPatient);
            communityPostsButton.setManaged(isPatient);
        }
        
        if (createRapportButton != null) {
            createRapportButton.setVisible(isMedecin);
            createRapportButton.setManaged(isMedecin);
        }
        
        if (viewRapportsButton != null) {
            viewRapportsButton.setVisible(isMedecin);
            viewRapportsButton.setManaged(isMedecin);
        }
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


    @FXML
    public void navigateToPostsList() {
        try {
            User currentUser = Session.getInstance().getUtilisateurConnecte();
            if (currentUser == null) {
                showError("No user logged in");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-list.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the user
            PostListController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) contentTabPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OncoKidsCare - Posts");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Error loading posts list: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();


    }


    @FXML
    public void navigateToPostList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-list.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the current user
            PostListController controller = loader.getController();
            controller.setCurrentUser(Session.getInstance().getUtilisateurConnecte());
            
            // Create a new scene and stage
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Community Posts");
            stage.setScene(scene);
            
            // Show the new stage
            stage.show();
            
            // Close the current window (optional)
            // ((Stage) communityPostsButton.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            showError("Error loading posts list: " + e.getMessage());
        }
    }
    
    // Add a method to handle the tab selection
    @FXML
    public void handleCommunityPostsTab() {
        navigateToPostList();
    }

    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/profile.fxml"));
            Parent profileRoot = loader.load();

            // Pass the current user to the ProfileController
            ProfileController profileController = loader.getController();
            // Use the session user if available, otherwise fallback to currentUser
            User user = Session.getInstance().getUtilisateurConnecte();
            if (user == null) user = currentUser;
            profileController.initData(user);

            // Create a new stage for the profile
            Stage profileStage = new Stage();
            profileStage.setTitle("User Profile");
            profileStage.setScene(new Scene(profileRoot));
            profileStage.setResizable(false);
            profileStage.show();
        } catch (IOException e) {
            showError("Error loading profile: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void updateUserInterface(User updatedUser) {
        if (updatedUser == null) return;
        
        // Update the current user reference
        this.currentUser = updatedUser;
        
        // Update UI elements
        welcomeLabel.setText("Welcome, " + updatedUser.getNom() + " " + updatedUser.getPrenom());
        nameLabel.setText("Name: " + updatedUser.getNom() + " " + updatedUser.getPrenom());
        emailLabel.setText("Email: " + updatedUser.getEmail());
        phoneLabel.setText("Phone: " + updatedUser.getTel());
        addressLabel.setText("Address: " + updatedUser.getAdresse());
        
        // Update session test label
        User sessionUser = Session.getInstance().getUtilisateurConnecte();
        if (sessionUser != null) {
            sessionTestLabel.setText("Session User: " + sessionUser.getEmail());
        }
        
        // Update role-specific content
        setupRoleSpecificContent();
    }}