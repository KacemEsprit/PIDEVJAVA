package com.pfe.nova.Controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.*;
import com.pfe.nova.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import com.pfe.nova.configuration.UserDAO;
import java.io.IOException;
import java.sql.*;
import javafx.application.HostServices;  // Use JavaFX HostServices instead of AWT Desktop
import java.net.URI;
import java.util.Random;
// Remove this import
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import javafx.application.Platform;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;



public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button googleLoginButton;
    @FXML private Label errorLabel;

    // Google OAuth configuration
    private final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private final String GOOGLE_CLIENT_ID = "100768907086-65bq2bedrqenm7nq3e4qd6noft8qulb8.apps.googleusercontent.com";
    private final String GOOGLE_CLIENT_SECRET = "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";
    private final String GOOGLE_REDIRECT_URI = "http://localhost:8085/oauth2callback";
    private final String GOOGLE_SCOPE = "email profile";
    private String stateToken;

    @FXML
    public void initialize() {

        googleLoginButton.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    // Add a field to store the HostServices instance
    private HostServices hostServices;

    // Method to set the HostServices (call this from your main application class)
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void handleGoogleLogin() {
        try {
            stateToken = generateStateToken();
            String authUrl = GOOGLE_AUTH_URL +
                    "?client_id=" + GOOGLE_CLIENT_ID +
                    "&redirect_uri=" + java.net.URLEncoder.encode(GOOGLE_REDIRECT_URI, "UTF-8") +
                    "&scope=" + java.net.URLEncoder.encode(GOOGLE_SCOPE, "UTF-8") +
                    "&response_type=code" +
                    "&state=" + stateToken;

            if (hostServices != null) {
                hostServices.showDocument(authUrl);
            } else {
                try {
                    java.awt.Desktop.getDesktop().browse(new URI(authUrl));
                } catch (Exception e) {
                    showError("Could not open browser: " + e.getMessage());
                }
            }

            startOAuthListener();
        } catch (Exception e) {
            showError("Error initiating Google login: " + e.getMessage());
        }
    }




    private void startOAuthListener() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0);
            server.createContext("/oauth2callback", exchange -> {
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

                String responseHtml;
                if (state != null && state.equals(stateToken) && code != null) {
                    responseHtml = "<html><body><h1>Authentication Successful</h1><script>window.close();</script></body></html>";
                    String finalCode = code;
                    Platform.runLater(() -> processGoogleAuthCode(finalCode)); // Pass the code here
                } else {
                    responseHtml = "<html><body><h1>Authentication Failed</h1></body></html>";
                }

                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, responseHtml.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseHtml.getBytes());
                }

                server.stop(0);
            });

            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            showError("Error starting OAuth listener: " + e.getMessage());
        }
    }


   private void processGoogleAuthCode(String code) {
        try {
            // Use Google API Client to exchange the authorization code for an access token
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            // Exchange auth code for access token
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    httpTransport,
                    jsonFactory,
                    GOOGLE_CLIENT_ID,
                    GOOGLE_CLIENT_SECRET,
                    code,
                    GOOGLE_REDIRECT_URI)
                    .execute();

            // Get access token from response - use the correct method based on your library version
            String accessToken = tokenResponse.getAccessToken();

            // Instead of using Oauth2 class which has compatibility issues,
            // use a direct HTTP request to get user info
            String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
            com.google.api.client.http.HttpRequestFactory requestFactory = 
                httpTransport.createRequestFactory(request -> 
                    request.getHeaders().setAuthorization("Bearer " + accessToken));
            
            com.google.api.client.http.GenericUrl url = new com.google.api.client.http.GenericUrl(userInfoUrl);
            com.google.api.client.http.HttpRequest request = requestFactory.buildGetRequest(url);
            com.google.api.client.http.HttpResponse response = request.execute();
            
            // Parse the JSON response - use the GoogleUserInfo from models package
            com.google.api.client.json.JsonObjectParser parser = new com.google.api.client.json.JsonObjectParser(jsonFactory);
            GoogleUserInfo userInfo = parser.parseAndClose(response.getContent(), 
                                                          response.getContentCharset(), 
                                                          GoogleUserInfo.class);

            // Extract user details
            String googleEmail = userInfo.getEmail();
            String firstName = userInfo.getGivenName();
            String lastName = userInfo.getFamilyName();
            String pictureUrl = userInfo.getPicture();

            // Check if user exists in database
            Platform.runLater(() -> {
                try {
                    // Check if user exists by email
                    User existingUser = UserDAO.findUserByEmail(googleEmail);
                    
                    if (existingUser != null) {
                        // User exists, log them in
                        Session.setUtilisateurConnecte(existingUser);
                        navigateToDashboard(existingUser);
                        showInfo("Welcome back, " + existingUser.getNom() + " " + existingUser.getPrenom());
                    } else {
                        navigateToGoogleSignup(googleEmail, firstName, lastName);
                    }
                } catch (Exception e) {
                    showError("Error processing login: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            Platform.runLater(() -> showError("Error processing Google authentication: " + e.getMessage()));
            e.printStackTrace();
        }
    }
    private void prefillGoogleUserInfo(String email, String firstName, String lastName) {
        // Example implementation
        System.out.println("Prefilling user info: " + email + ", " + firstName + ", " + lastName);
    }

    private void navigateToGoogleSignup(String email, String firstName, String lastName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/signup.fxml"));
            Parent root = loader.load();
            
            SignupController signupController = loader.getController();
            // Pass HostServices to the signup controller
            signupController.setHostServices(this.hostServices);
            // Pre-fill the form with Google user info
            signupController.prefillGoogleUserInfo(email, firstName, lastName);
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Complete Your Profile");
            stage.centerOnScreen();
            
        } catch (IOException e) {
            showError("Unable to load signup page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        User user = authenticateUser(email, password);
        if (user != null) {
            System.out.println("Authenticated user: " + user.getEmail() + ", Role: " + user.getRole());
            Session.setUtilisateurConnecte(user);
            navigateToDashboard(user);
        } else {
            showError("Invalid email or password");
            System.out.println("Authentication failed for email: " + email);
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

    @FXML
    private void navigateToForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/forgot-password.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Forgot Password");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to load forgot password page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(User user) {
        try {
            System.out.println("Navigating to dashboard for role: " + user.getRole());
            String dashboardPath;
    
            if ("ROLE_ADMIN".equals(user.getRole()) || "ADMIN".equals(user.getRole())) {
                dashboardPath = "/com/pfe/novaview/admin-dashboard.fxml";
            } else {
                dashboardPath = "/com/pfe/novaview/dashboard.fxml";
            }
    
            FXMLLoader loader = new FXMLLoader(getClass().getResource(dashboardPath));
            Parent root = loader.load();
    
            if ("ROLE_ADMIN".equals(user.getRole()) || "ADMIN".equals(user.getRole())) {
                AdminDashboardController adminController = loader.getController();
                adminController.initData(user);
            } else {
                DashboardController dashboardController = loader.getController();
                dashboardController.initData(user);
            }
    
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root,1200,800));
            stage.setTitle(user.getRole() + " Dashboard");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private User authenticateUser(String email, String password) {
        return UserDAO.authenticateUser(email, password);
    }



    private String generateStateToken() {
        // Generate a random string for state verification
        Random random = new Random();
        return String.valueOf(random.nextInt(1000000));
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
    
    private void showInfo(String message) {
        errorLabel.setStyle("-fx-text-fill: blue;");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}