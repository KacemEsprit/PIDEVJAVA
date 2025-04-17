package com.pfe.nova.Controller;

import com.pfe.nova.configuration.RapportDAO;
import com.pfe.nova.models.Rapport;
import com.pfe.nova.models.User;
import com.pfe.nova.utils.Session;
import com.pfe.nova.configuration.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private StackPane contentArea;
    @FXML private TabPane mainTabPane;
    @FXML private TableView<User> usersTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterRole;
    
    private User adminUser;
    
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> actionsColumn;
    @FXML
    private GridPane reportsGridPane;
    
    @FXML
    public void initialize() {
        setupUI();
        setupTableColumns();
        loadUsersData(); // Add this line to load data when initializing
    }
    
    private void setupTableColumns() {
        try {
            idColumn.setCellValueFactory(cellData -> 
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
            
            nameColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getNom() + " " + cellData.getValue().getPrenom()));
            
            emailColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getEmail()));
            
            roleColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getRole()));
            
            actionsColumn.setCellFactory(column -> new TableCell<User, String>() {
                private final Button deleteButton = new Button("Delete");
                private final Button editButton = new Button("Edit");
                private final HBox buttons = new HBox(5, editButton, deleteButton);
                
                {
                    deleteButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        if (confirmDelete(user)) {
                            UserDAO.deleteUser(user.getId());
                            loadUsersData(); // Refresh the table
                        }
                    });

                    editButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        handleEditUser(user);
                    });
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttons);
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error setting up table columns: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void initData(User user) {
        if (!"ADMIN".equals(user.getRole())) {
            // Redirect non-admin users
            System.err.println("Non-admin user attempted to access admin dashboard");
            handleLogout();
            return;
        }
        
        this.adminUser = user;
        welcomeLabel.setText("Welcome, " + user.getNom() + " " + user.getPrenom());
        loadUsersData();
    }
    
    private void setupUI() {
        filterRole.getItems().addAll("ALL", "ADMIN", "MEDECIN", "PATIENT", "DONATEUR");
        filterRole.setValue("ALL");
        
        // Add search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });
        
        // Add role filter listener
        filterRole.valueProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });
    }
    
    @FXML
    private void handleLogout() {
        try {
            Session.logout();  // Changed from clearSession() to logout()
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Error during logout: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String selectedRole = filterRole.getValue();
        // TODO: Implement search functionality with UserDAO
    }
    
    @FXML
    private void handleAddUser() {
        // TODO: Implement add user functionality
    }
    
    @FXML
    private void showUsersManagement() {
        mainTabPane.getSelectionModel().select(0);
    }
    
    @FXML
    private void showStatistics() {
        // TODO: Implement statistics view
    }

    @FXML
    private void showReports() {
        try {
            // Clear existing content in the GridPane
            reportsGridPane.getChildren().clear();

            // Add header row
            reportsGridPane.add(new Label("ID"), 0, 0);
            reportsGridPane.add(new Label("Patient ID"), 1, 0);
            reportsGridPane.add(new Label("Age"), 2, 0);
            reportsGridPane.add(new Label("Date"), 3, 0);
            reportsGridPane.add(new Label("Sexe"), 4, 0);
            reportsGridPane.add(new Label("Tension"), 5, 0);
            reportsGridPane.add(new Label("Pouls"), 6, 0);
            reportsGridPane.add(new Label("Température"), 7, 0);
            reportsGridPane.add(new Label("Saturation"), 8, 0);
            reportsGridPane.add(new Label("IMC"), 9, 0);
            reportsGridPane.add(new Label("Niveau Douleur"), 10, 0);
            reportsGridPane.add(new Label("Traitement"), 11, 0);
            reportsGridPane.add(new Label("Dose"), 12, 0);
            reportsGridPane.add(new Label("Fréquence"), 13, 0);
            reportsGridPane.add(new Label("Perte de Sang"), 14, 0);
            reportsGridPane.add(new Label("Temps Opération"), 15, 0);
            reportsGridPane.add(new Label("Durée Séance"), 16, 0);
            reportsGridPane.add(new Label("Filtration"), 17, 0);
            reportsGridPane.add(new Label("Créatinine"), 18, 0);
            reportsGridPane.add(new Label("Glasgow"), 19, 0);
            reportsGridPane.add(new Label("Respiration"), 20, 0);
            reportsGridPane.add(new Label("Complications"), 21, 0);

            // Fetch reports from the database
            RapportDAO rapportDAO = new RapportDAO();
            List<Rapport> rapports = rapportDAO.getAlls();

            // Populate the GridPane with report data
            for (int i = 0; i < rapports.size(); i++) {
                Rapport rapport = rapports.get(i);
                reportsGridPane.add(new Label(String.valueOf(rapport.getId())), 0, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getPatientId())), 1, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getAge())), 2, i + 1);
                reportsGridPane.add(new Label(rapport.getDateRapport()), 3, i + 1);
                reportsGridPane.add(new Label(rapport.getSexe()), 4, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getTensionArterielle())), 5, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getPouls())), 6, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getTemperature())), 7, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getSaturationOxygene())), 8, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getImc())), 9, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getNiveauDouleur())), 10, i + 1);
                reportsGridPane.add(new Label(rapport.getTraitement()), 11, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getDoseMedicament())), 12, i + 1);
                reportsGridPane.add(new Label(rapport.getFrequenceTraitement()), 13, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getPerteDeSang())), 14, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getTempsOperation())), 15, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getDureeSeance())), 16, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getFiltrationSang())), 17, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getCreatinine())), 18, i + 1);
                reportsGridPane.add(new Label(String.valueOf(rapport.getScoreGlasgow())), 19, i + 1);
                reportsGridPane.add(new Label(rapport.isRespirationAssistee() == 1 ? "Oui" : "Non"), 20, i + 1);
                reportsGridPane.add(new Label(rapport.getComplications()), 21, i + 1);
            }

            // Show the GridPane and hide other content
            reportsGridPane.setVisible(true);
            mainTabPane.setVisible(false);
        } catch (Exception e) {
            showError( "Failed to load reports: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void showSettings() {
        // TODO: Implement settings view
    }
    
    private void loadUsersData() {
        // TODO: Implement this in UserDAO
        List<User> users = UserDAO.getAllUsers();
        usersTable.getItems().clear();
        usersTable.getItems().addAll(users);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/edituser.fxml"));
            Parent root = loader.load();

            // Pass the selected user to the EditUserController
            EditUserController editUserController = loader.getController();
            editUserController.initData(user);

            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error loading Edit User page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private boolean confirmDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Are you sure you want to delete this user?");
        alert.setContentText("User: " + user.getNom() + " " + user.getPrenom());

        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }
}