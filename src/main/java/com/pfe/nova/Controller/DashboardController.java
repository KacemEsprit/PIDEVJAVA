package com.pfe.nova.Controller;

import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.*;
import com.pfe.nova.services.CompagnieService;
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
            // Change this line to get the Session instance first
            User currentUser = Session.getInstance().getUtilisateurConnecte();
            boolean isMedecin = currentUser instanceof Medecin;
            
            // Show the community posts button only for patients
            if (currentUser != null && "PATIENT".equals(currentUser.getRole())) {
                communityPostsButton.setVisible(true);
                // Enable the community posts tab for patients
                if (communityPostsTab != null) {
                    communityPostsTab.setDisable(false);
                }
            } else {
                // Disable the tab for non-patients
                if (communityPostsTab != null) {
                    communityPostsTab.setDisable(true);
                }
            }


            createRapportTab.setDisable(!isMedecin);
            viewRapportTab.setDisable(!isMedecin);

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
        
        // Show/hide admin posts management button based on role
        if (adminPostsBtn != null) {
            boolean isAdmin = user.getRole() != null && user.getRole().toUpperCase().contains("ADMIN");
            adminPostsBtn.setVisible(isAdmin);
            adminPostsBtn.setManaged(isAdmin); // This removes the space when button is hidden
        }
    }

    @FXML private Button createRapportButton;
    @FXML private Button viewRapportsButton;
    @FXML private Button faireDonButton;
    @FXML private Button historiqueDonButton;

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


        User sessionUser = Session.getInstance().getUtilisateurConnecte();
        if (sessionUser != null) {
            sessionTestLabel.setText("Session User: " + sessionUser.getEmail());
        } else {
            sessionTestLabel.setText("No user in session.");
        }


        if (patientsTab != null) patientsTab.setDisable(!(currentUser instanceof Medecin));
        if (findDoctorsTab != null) findDoctorsTab.setDisable(!(currentUser instanceof Patient));
        if (adminTab != null) adminTab.setDisable(!(currentUser.getRole().equals("ADMIN")));
        if (donationsTab != null) {
            donationsTab.setDisable(!(currentUser instanceof Donateur));
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
    private void handleFaireDon() {
        try {
            String fxmlPath;
            String title;
            if (currentUser instanceof Donateur donateur && "individuel".equalsIgnoreCase(donateur.getDonateurType())) {
                fxmlPath = "/com/pfe/novaview/Don/AjouterDon.fxml";
                title = "Faire un Don";
            } else {
                fxmlPath = "/com/pfe/novaview/Compagnie/AjouterCompagnie.fxml";
                title = "Ajouter une Compagnie";
            }
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) faireDonButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de l'interface: " + e.getMessage());
        }
    }

    @FXML
    private void handleHistoriqueDon() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/pfe/novaview/Don/AfficherDon.fxml"));
            Stage stage = (Stage) historiqueDonButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Historique des Dons");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de l'historique des dons: " + e.getMessage());
        }
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

    /**
     * Navigate to the post list view
     */
    @FXML
    public void navigateToPostList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-list.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the current user
            PostListController controller = loader.getController();
            controller.setCurrentUser(Session.getCurrentUser());
            
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not navigate to Posts");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    // Add a method to handle the tab selection
    @FXML
    public void handleCommunityPostsTab() {
        navigateToPostList();
    }
}
