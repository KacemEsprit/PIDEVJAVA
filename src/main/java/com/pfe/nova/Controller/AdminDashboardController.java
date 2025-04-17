package com.pfe.nova.Controller;

import com.pfe.nova.models.Donateur;
import com.pfe.nova.models.Medecin;
import com.pfe.nova.models.Patient;
import com.pfe.nova.models.User;
import com.pfe.nova.utils.Session;
import com.pfe.nova.configuration.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.io.IOException;
import java.util.List;

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
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Delete");
                private final HBox buttons = new HBox(5, editButton, deleteButton);

                {
                    // Set action for the Edit button
                    editButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        handleEditUser(user);
                    });

                    // Set action for the Delete button
                    deleteButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        if (confirmDelete(user)) {
                            UserDAO.deleteUser(user.getId());
                            loadUsersData(); // Refresh the table
                        }
                    });

                    // Style buttons (optional)
                    editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
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
        // TODO: Implement reports view
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
            // Get the full user data with role-specific information
            User fullUser = UserDAO.getUserById(user.getId());
            if (fullUser == null) {
                showError("Could not load user data");
                return;
            }

            // Debug print to check user data
            System.out.println("Opening edit window for user: " + fullUser.getRole());

            // Use absolute path for FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/edituser.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("Could not find edituser.fxml");
                showError("Could not find edit user form");
                return;
            }

            Parent root = loader.load();
            EditUserController editController = loader.getController();
            if (editController == null) {
                System.err.println("Could not get EditUserController");
                showError("Error initializing edit form");
                return;
            }

            editController.initData(fullUser);

            Stage stage = new Stage();
            stage.setTitle("Edit User - " + fullUser.getRole());
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading edit window: " + e.getMessage());
            e.printStackTrace();
            showError("Error opening edit window: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showError("Unexpected error: " + e.getMessage());
        }
    }

    private boolean confirmDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete user: " + user.getNom() + " " + user.getPrenom() + "?");

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/profile.fxml"));
            Parent root = loader.load();
            
            ProfileController profileController = loader.getController();
            profileController.initData(this.adminUser);
            
            Stage stage = new Stage();
            stage.setTitle("My Profile");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error opening profile: " + e.getMessage());
        }
    }}