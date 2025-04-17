package com.pfe.nova.Controller;

import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.configuration.UserDAO;
import com.pfe.nova.models.*;
import com.pfe.nova.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        setupButtonHoverEffects();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        try {
            User user = authenticateUser(email, password);
            if (user != null) {
                // Store the connected user in the session
                Session.setUtilisateurConnecte(user);
                System.out.println("User logged in: " + user.getEmail());
                System.out.println("User role: " + user.getRole());

                // Check role and navigate accordingly
                String role = user.getRole();
                if (role != null) {
                    role = role.toUpperCase();
                    // Use contains instead of equals to handle both formats
                    if (role.contains("ADMIN")) {
                        navigateToAdminDashboard(user);
                    } else {
                        navigateToUserDashboard(user);
                    }
                } else {
                    showError("User role is not defined");
                }
            } else {
                showError("Invalid email or password");
            }
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToAdminDashboard(User user) {
        try {
            // Make sure the user is stored in the session
            Session.setUtilisateurConnecte(user);
            System.out.println("Setting admin user in session: " + user.getEmail() + ", role: " + user.getRole());
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/admin-posts-management.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the user
            AdminPostsManagementController controller = loader.getController();
            if (controller != null) {
                controller.setCurrentUser(user);
            } else {
                System.err.println("Warning: AdminPostsManagementController is null");
            }
            
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            showError("Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToUserDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-list.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the user
            PostListController controller = loader.getController();
            controller.setCurrentUser(user);
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OncoKidsCare - Posts");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/signup.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find signup.fxml");
            }
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Sign Up");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to load signup page: " + e.getMessage());
            System.err.println("Error loading signup page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(User user) {
        try {
            System.out.println("Loading dashboard for user role: " + user.getRole());
            String dashboardPath = "/com/pfe/novaview/dashboard.fxml";  // Fixed path to match project structure
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(dashboardPath));
            if (loader.getLocation() == null) {
                System.err.println("Dashboard FXML not found at: " + dashboardPath);
                throw new IOException("Cannot find dashboard.fxml at " + dashboardPath);
            }
            
            Parent root = loader.load();
            DashboardController dashboardController = loader.getController();
            
            if (dashboardController == null) {
                throw new IOException("Failed to get DashboardController instance");
            }
            
            dashboardController.initData(user);
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(user.getRole() + " Dashboard");
            stage.setResizable(true);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            showError("Unable to load dashboard: " + e.getMessage());
            System.err.println("Dashboard loading error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private User authenticateUser(String email, String password) {
        try {
            // Get the user by email
            User user = UserDAO.findByEmail(email);
            
            if (user != null) {
                System.out.println("Found user: " + user.toString());
                System.out.println("User role from DB: " + user.getRole());
                
                // Check if password matches
                if (BCrypt.checkpw(password, user.getPassword())) {
                    return user;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void setupButtonHoverEffects() {
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle("-fx-background-color: #2980b9; -fx-pref-width: 300px; -fx-pref-height: 40px; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 3;")
        );
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle("-fx-background-color: #3498db; -fx-pref-width: 300px; -fx-pref-height: 40px; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 3;")
        );
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> errorLabel.setVisible(false));
                    }
                },
                5000
        );
    }
}