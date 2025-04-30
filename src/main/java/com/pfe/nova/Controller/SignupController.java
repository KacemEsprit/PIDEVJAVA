package com.pfe.nova.Controller;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import com.pfe.nova.configuration.UserDAO;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.application.HostServices;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import javafx.application.Platform;
// Fix this import if GoogleAuthCodeController is in a different package
import com.pfe.nova.Controller.GoogleAuthCodeController;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

public class SignupController {
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telField;
    @FXML private TextField adresseField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private VBox dynamicFieldsContainer;
    @FXML private Label errorLabel;
    @FXML private Button signupButton;
    @FXML private Button googleSignupButton;
    
    // Google OAuth configuration
    private final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
  //  private final String GOOGLE_CLIENT_ID = "100768907086-65bq2bedrqenm7nq3e4qd6noft8qulb8.apps.googleusercontent.com";
  //  private final String GOOGLE_CLIENT_SECRET = "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";
    private final String GOOGLE_REDIRECT_URI = "http://localhost:8085/oauth2callback";
    private final String GOOGLE_SCOPE = "email profile";
    private String stateToken;
    private boolean isGoogleSignup = false;
    
    // Add a field to store the HostServices instance
    private HostServices hostServices;
    
    // Method to set the HostServices (call this from your main application class)
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    // Dynamic fields for different roles
    private TextField specialiteField;
    private TextField experienceField;
    private TextField diplomeField;
    private TextField ageField;
    private ComboBox<String> genderComboBox;
    private TextField bloodTypeField;
    private TextField donateurTypeField;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("ROLE_MEDECIN", "ROLE_PATIENT", "ROLE_DONATEUR");
        roleComboBox.setOnAction(e -> updateDynamicFields());
        
        
        // Style the Google button
        googleSignupButton.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // Make error label visible but initially empty
        errorLabel.setVisible(true);
        errorLabel.setText("");
    }
    
    @FXML
  /*  private void handleGoogleSignup() {
        try {

            
            // Build the Google OAuth URL with proper URL encoding
            String authUrl = GOOGLE_AUTH_URL + 
                 "?client_id=" + GOOGLE_CLIENT_ID + 
                "&redirect_uri=" + java.net.URLEncoder.encode(GOOGLE_REDIRECT_URI, "UTF-8") + 
                "&scope=" + java.net.URLEncoder.encode(GOOGLE_SCOPE, "UTF-8") + 
                "&response_type=code" + 
                "&state=" + stateToken;
            
            // Open the default browser with the Google OAuth URL using HostServices
            if (hostServices != null) {
                hostServices.showDocument(authUrl);
            } else {
                // Fallback to using Desktop if HostServices is not available
                try {
                    // Use reflection to avoid direct Desktop dependency
                    Class<?> desktopClass = Class.forName("java.awt.Desktop");
                    Object desktop = desktopClass.getMethod("getDesktop").invoke(null);
                    desktopClass.getMethod("browse", URI.class).invoke(desktop, new URI(authUrl));
                } catch (Exception e) {
                    throw new IOException("Could not open browser: " + e.getMessage());
                }
            }
            
            // Show a message to the user
            showInfo("Please sign up with Google in your browser. You will be redirected back to the application.");
            
            // Start a listener for the OAuth callback
            startOAuthListener();
            
        } catch (IOException e) {  // Removed URISyntaxException
            showError("Error opening browser: " + e.getMessage());
            e.printStackTrace();
        }
    }*/
    

    
private void startOAuthListener() {
    try {
        // Create HTTP server with backlog of 0 for default value
        HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0); // Changed port from 8080 to 8085
        
        // Create context for handling OAuth callback
        server.createContext("/oauth2callback", exchange -> {
            try {
                // Add these headers to prevent authentication prompt
                exchange.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"localhost\"");
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                
                // Parse query parameters
                String query = exchange.getRequestURI().getQuery();
                String code = null;
                String state = null;
                
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] pair = param.split("=");
                        if (pair.length == 2) {
                            if ("code".equals(pair[0])) {
                                code = java.net.URLDecoder.decode(pair[1], "UTF-8");
                            } else if ("state".equals(pair[0])) {
                                state = java.net.URLDecoder.decode(pair[1], "UTF-8");
                            }
                        }
                    }
                }

                // Prepare response
                String responseHtml;
                int responseCode;

                // Validate state and code
                if (state != null && state.equals(stateToken) && code != null) {
                    responseHtml = "<html><body><h1>Authentication Successful</h1>"
                               + "<p>You can close this window and return to the application.</p>"
                               + "<script>window.close();</script></body></html>";
                    responseCode = 200;
                    
                    // Process auth code on JavaFX thread
                    final String authCode = code;
                    Platform.runLater(() -> processGoogleAuthCode(authCode));
                } else {
                    responseHtml = "<html><body><h1>Authentication Failed</h1>"
                               + "<p>Invalid state token or missing authorization code.</p></body></html>";
                    responseCode = 400;
                }

                // Send response
                byte[] responseBytes = responseHtml.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(responseCode, responseBytes.length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }

            } catch (Exception e) {
                Platform.runLater(() -> showError("Error processing callback: " + e.getMessage()));
                exchange.sendResponseHeaders(500, 0);
            } finally {
                exchange.close();
                // Stop server after handling request
                server.stop(1);
            }
        });

        // Start server
        server.setExecutor(null);
        server.start();

        Platform.runLater(() -> showInfo("Waiting for Google authentication..."));

    } catch (IOException e) {
        Platform.runLater(() -> {
            showError("Failed to start OAuth listener: " + e.getMessage());
            e.printStackTrace();
            showManualCodeEntryDialog();
        });
    }
}

    private void showManualCodeEntryDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/code-entry.fxml"));
            Parent root = loader.load();
            
            GoogleAuthCodeController codeController = loader.getController();
            codeController.setCallback(this::processGoogleAuthCode);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Enter Google Auth Code");
            stage.show();
            
        } catch (IOException e) {
            showError("Error loading code entry page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void processGoogleAuthCode(String code) {
//        try {
//            // Use Google API Client to exchange the authorization code for an access token
//            HttpTransport httpTransport = new NetHttpTransport();
//            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//
//            // Exchange auth code for access token
//            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
//                    httpTransport,
//                    jsonFactory,
//                    GOOGLE_CLIENT_ID,
//                    GOOGLE_CLIENT_SECRET,
//                    code,
//                    GOOGLE_REDIRECT_URI)
//                    .execute();
//
//            // Get access token from response
//            String accessToken = tokenResponse.getAccessToken();
//
//            // Use the access token to get user info
//            Oauth2 oauth2 = new Oauth2.Builder(
//                    httpTransport,
//                    jsonFactory,
//                    request -> request.getHeaders().set("Authorization", "Bearer " + accessToken))
//                    .setApplicationName("Nova Application")
//                    .build();
//
//            // Get user info from Google
//            Userinfo userInfo = oauth2.userinfo().get().execute();
//
//            // Extract user details
//            String googleEmail = userInfo.getEmail();
//            String firstName = userInfo.getGivenName();
//            String lastName = userInfo.getFamilyName();
//
//            // Pre-fill the form with Google user info
//            prefillGoogleUserInfo(googleEmail, firstName, lastName);
//
//            // Mark this as a Google signup
//            isGoogleSignup = true;
//
//        } catch (Exception e) {
//            showError("Error processing Google authentication: " + e.getMessage());
//            e.printStackTrace();
//        }
    }
    
    public void prefillGoogleUserInfo(String email, String firstName, String lastName) {
        Platform.runLater(() -> {
            emailField.setText(email);
            nomField.setText(firstName);
            prenomField.setText(lastName);
            
            // Disable email field since it's coming from Google
            emailField.setDisable(true);
            
            // If this is a Google signup, we might want to hide the password fields
            if (isGoogleSignup) {
                passwordField.setVisible(false);
                confirmPasswordField.setVisible(false);
                // You might want to add labels to indicate this is a Google signup
                showInfo("Please complete your profile information");
            }
        });
    }
    
    // You'll also need to update the updateDynamicFields method to handle the new role names
    private void updateDynamicFields() {
        String selectedRole = roleComboBox.getValue();
        dynamicFieldsContainer.getChildren().clear();
        
        if (selectedRole == null) return;
        
        switch (selectedRole) {
            case "ROLE_MEDECIN":
                specialiteField = new TextField();
                experienceField = new TextField();
                diplomeField = new TextField();
                
                specialiteField.setPromptText("Specialité");
                experienceField.setPromptText("Experience");
                diplomeField.setPromptText("Diplome");
                
                dynamicFieldsContainer.getChildren().addAll(
                    specialiteField, experienceField, diplomeField
                );
                break;

            case "ROLE_PATIENT":
                ageField = new TextField();
                genderComboBox = new ComboBox<>();
                bloodTypeField = new TextField();
                
                ageField.setPromptText("Age");
                genderComboBox.setPromptText("Gender");
                genderComboBox.getItems().addAll("Male", "Female");
                bloodTypeField.setPromptText("Blood Type");
                
                dynamicFieldsContainer.getChildren().addAll(
                    ageField, genderComboBox, bloodTypeField
                );
                break;

            case "ROLE_DONATEUR":
                donateurTypeField = new TextField();
                donateurTypeField.setPromptText("Donateur Type");
                dynamicFieldsContainer.getChildren().add(donateurTypeField);
                break;
        }

        // Apply consistent styling to all dynamic fields
        dynamicFieldsContainer.getChildren().forEach(node -> {
            if (node instanceof TextField || node instanceof ComboBox) {
                node.setStyle("-fx-pref-width: 300px; -fx-pref-height: 40px");
            }
        });
    }

    @FXML
    private void handleSignup() throws SQLException {
        // Basic validation
        if (!validateFields()) return;

        // Check if email already exists (skip for Google users as we've already verified)
        if (!isGoogleSignup && UserDAO.isEmailExists(emailField.getText())) {
            showError("Email already registered");
            return;
        }

        // Email format validation
        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format");
            return;
        }

        try {
            // Generate a salt and hash the password
            String hashedPassword = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt(12));
            User user = createUserBasedOnRole(hashedPassword);

            if (user != null && UserDAO.registerUser(user)) {
                showSuccess("Registration successful!");
                navigateToLogin(new ActionEvent());
            }
        } catch (IllegalArgumentException e) {
            showError("Error processing password");
            e.printStackTrace();
        }
    }

    private void showSuccess(String message) {
        errorLabel.setStyle("-fx-text-fill: green;");
        showError(message); // Reusing the showError method for success messages
    }

    private boolean validateFields() {
        // For Google sign-ups, we don't need to validate password fields
        if (isGoogleSignup) {
            if (nomField.getText().isEmpty() || 
                prenomField.getText().isEmpty() || 
                emailField.getText().isEmpty() || 
                telField.getText().isEmpty() || 
                adresseField.getText().isEmpty() || 
                roleComboBox.getValue() == null) {
                
                showError("Please fill in all required fields");
                return false;
            }
        } else {
            // Regular validation for non-Google sign-ups
            if (nomField.getText().isEmpty() || 
                prenomField.getText().isEmpty() || 
                emailField.getText().isEmpty() || 
                telField.getText().isEmpty() || 
                adresseField.getText().isEmpty() || 
                passwordField.getText().isEmpty() || 
                confirmPasswordField.getText().isEmpty() || 
                roleComboBox.getValue() == null) {
                
                showError("Please fill in all required fields");
                return false;
            }

            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                showError("Passwords do not match");
                return false;
            }
        }

        // Validate role-specific fields
        String role = roleComboBox.getValue();
        switch (role) {
            case "ROLE_MEDECIN":
                if (specialiteField.getText().isEmpty() || 
                    experienceField.getText().isEmpty() || 
                    diplomeField.getText().isEmpty()) {
                    showError("Please fill in all medical professional fields");
                    return false;
                }
                break;
            case "ROLE_PATIENT":
                if (ageField.getText().isEmpty() || 
                    genderComboBox.getValue() == null || 
                    bloodTypeField.getText().isEmpty()) {
                    showError("Please fill in all patient fields");
                    return false;
                }
                try {
                    Integer.parseInt(ageField.getText());
                } catch (NumberFormatException e) {
                    showError("Age must be a valid number");
                    return false;
                }
                break;
            case "ROLE_DONATEUR":
                if (donateurTypeField.getText().isEmpty()) {
                    showError("Please specify donor type");
                    return false;
                }
                break;
        }
        return true;
    }

    private User createUserBasedOnRole(String hashedPassword) {
        String role = roleComboBox.getValue();
        if (role == null) return null;

        switch (role) {
            case "ROLE_MEDECIN":
                return new Medecin(
                    0, // ID will be set by database
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    telField.getText(),
                    adresseField.getText(),
                    hashedPassword,
                    "", // picture placeholder
                    specialiteField.getText(),
                    experienceField.getText(),
                    diplomeField.getText()
                );

            case "ROLE_PATIENT":
                return new Patient(
                    0,
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    telField.getText(),
                    adresseField.getText(),
                    hashedPassword,
                    "",
                    Integer.parseInt(ageField.getText()),
                    genderComboBox.getValue(),
                    bloodTypeField.getText()
                );

            case "ROLE_DONATEUR":
                return new Donateur(
                    0,
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    telField.getText(),
                    adresseField.getText(),
                    hashedPassword,
                    "",
                    donateurTypeField.getText()
                );

            default:
                return null;
        }
    }

    private boolean registerUser(User user) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // First, insert the base user data
            String userQuery = "INSERT INTO user (nom, prenom, email, tel, adresse, password, picture, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement userStmt = connection.prepareStatement(userQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, user.getNom());
                userStmt.setString(2, user.getPrenom());
                userStmt.setString(3, user.getEmail());
                userStmt.setString(4, user.getTel());
                userStmt.setString(5, user.getAdresse());
                userStmt.setString(6, user.getPassword());
                userStmt.setString(7, user.getPicture());
                userStmt.setString(8, user.getRole());
                
                int affectedRows = userStmt.executeUpdate();
                
                if (affectedRows == 0) {
                    showError("Failed to create user");
                    return false;
                }

                // Get the generated user ID
                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        
                        // Handle specific fields based on user type
                        switch (user.getRole()) {
                            case "MEDECIN":
                                Medecin medecin = (Medecin) user;
                                String medecinQuery = "UPDATE user SET specialite = ?, experience = ?, diplome = ? WHERE id = ?";
                                try (PreparedStatement medecinStmt = connection.prepareStatement(medecinQuery)) {
                                    medecinStmt.setString(1, medecin.getSpecialite());
                                    medecinStmt.setString(2, medecin.getExperience());
                                    medecinStmt.setString(3, medecin.getDiplome());
                                    medecinStmt.setInt(4, userId);
                                    medecinStmt.executeUpdate();
                                }
                                break;

                            case "PATIENT":
                                Patient patient = (Patient) user;
                                String patientQuery = "UPDATE user SET age = ?, gender = ?, blood_type = ? WHERE id = ?";
                                try (PreparedStatement patientStmt = connection.prepareStatement(patientQuery)) {
                                    patientStmt.setInt(1, patient.getAge());
                                    patientStmt.setString(2, patient.getGender());
                                    patientStmt.setString(3, patient.getBloodType());
                                    patientStmt.setInt(4, userId);
                                    patientStmt.executeUpdate();
                                }
                                break;

                            case "DONATEUR":
                                Donateur donateur = (Donateur) user;
                                String donateurQuery = "UPDATE user SET donateur_type = ? WHERE id = ?";
                                try (PreparedStatement donateurStmt = connection.prepareStatement(donateurQuery)) {
                                    donateurStmt.setString(1, donateur.getDonateurType());
                                    donateurStmt.setInt(2, userId);
                                    donateurStmt.executeUpdate();
                                }
                                break;
                        }
                        return true;
                    } else {
                        showError("Failed to retrieve user ID");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }



    @FXML
    private void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/login.fxml"));
            Parent root = loader.load();
            
            // Get the stage from a known UI component (like signupButton) instead of the event
            Stage stage = (Stage) signupButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to load login page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showInfo(String message) {
        errorLabel.setStyle("-fx-text-fill: blue;");
        errorLabel.setText(message);
    }

    private void showError(String message) {
        errorLabel.setStyle("-fx-text-fill: red;");
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