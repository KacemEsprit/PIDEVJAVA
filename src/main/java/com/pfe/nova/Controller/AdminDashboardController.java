package com.pfe.nova.Controller;

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
import java.sql.SQLException; 
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
                private final Button deleteButton = new Button("Delete");
                private final Button editButton = new Button("Edit");
                private final HBox buttons = new HBox(5, editButton, deleteButton);
                
                {
                    deleteButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        if (confirmDelete(user)) {
                            try {
                                UserDAO.deleteUser(user.getId());
                                loadUsersData(); // Refresh the table
                            } catch (SQLException e) {
                                showError("Error deleting user: " + e.getMessage());
                                e.printStackTrace();
                            }
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
        // Change from Session.logout() to Session.getInstance().logout()
        Session.getInstance().logout();
        
        // Navigate to login screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            showError("Error navigating to login: " + e.getMessage());
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