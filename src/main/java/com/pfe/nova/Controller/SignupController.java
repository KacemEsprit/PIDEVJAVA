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
import com.pfe.nova.Controller.GoogleAuthCodeController;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;
import org.json.JSONObject;
import okhttp3.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    @FXML private WebView recaptchaWebView;
    @FXML private Label recaptchaStatusLabel;

    // Google OAuth configuration
    private final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private final String GOOGLE_CLIENT_ID = "100768907086-65bq2bedrqenm7nq3e4qd6noft8qulb8.apps.googleusercontent.com";
    private final String GOOGLE_CLIENT_SECRET = "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";
    private final String GOOGLE_REDIRECT_URI = "http://localhost:8085/oauth2callback";
    private final String GOOGLE_SCOPE = "email profile";
    private String stateToken;
    private boolean isGoogleSignup = false;

    // reCAPTCHA configuration
    private final String RECAPTCHA_SITE_KEY = "6LeUJysrAAAAALEGDj-HRGhbiBaIzIvwzXxwHB9d";
    private final String RECAPTCHA_SECRET_KEY = "6LeUJysrAAAAAL-aUfIH4VbHxtlhmAB9WJ6E4w81";

    private AtomicBoolean recaptchaVerified = new AtomicBoolean(false);
    private String recaptchaResponse = "";
    private HttpServer recaptchaServer;

    // Add a field to store the HostServices instance
    private HostServices hostServices;

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

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

        googleSignupButton.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold;");

        errorLabel.setVisible(true);
        errorLabel.setText("");

        initializeReCaptcha();
    }

    private void initializeReCaptcha() {
        try {
            recaptchaServer = HttpServer.create(new InetSocketAddress(8085), 0);
            recaptchaServer.createContext("/recaptcha", exchange -> {
                String html = "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "    <title>reCAPTCHA</title>" +
                        "    <script src='https://www.google.com/recaptcha/api.js' async defer></script>" +
                        "    <style>body { margin: 0; overflow: hidden; }</style>" +
                        "    <script type=\"text/javascript\">" +
                        "        function onRecaptchaSuccess(response) {" +
                        "            if (window.javaCallback) {" +
                        "                window.javaCallback.onCaptchaSuccess(response);" +
                        "            }" +
                        "        }" +
                        "        function onRecaptchaExpired() {" +
                        "            if (window.javaCallback) {" +
                        "                window.javaCallback.onCaptchaExpired();" +
                        "            }" +
                        "        }" +
                        "    </script>" +
                        "</head>" +
                        "<body>" +
                        "    <div class='g-recaptcha' " +
                        "         data-sitekey='" + RECAPTCHA_SITE_KEY + "' " +
                        "         data-callback='onRecaptchaSuccess' " +
                        "         data-expired-callback='onRecaptchaExpired'></div>" +
                        "</body>" +
                        "</html>";

                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, html.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(html.getBytes());
                }
                exchange.close();
            });

            recaptchaServer.createContext("/oauth2callback", exchange -> {
                try {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

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
                    int responseCode;

                    if (state != null && state.equals(stateToken) && code != null) {
                        responseHtml = "<html><body><h1>Authentication Successful</h1>"
                                + "<p>You can close this window and return to the application.</p>"
                                + "<script>window.close();</script></body></html>";
                        responseCode = 200;
                        final String authCode = code;
                        Platform.runLater(() -> processGoogleAuthCode(authCode));
                    } else {
                        responseHtml = "<html><body><h1>Authentication Failed</h1>"
                                + "<p>Invalid state token or missing authorization code.</p></body></html>";
                        responseCode = 400;
                    }

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
                }
            });

            recaptchaServer.setExecutor(null);
            recaptchaServer.start();
            System.out.println("Local server started on port 8085 for reCAPTCHA and OAuth callback");

            WebEngine engine = recaptchaWebView.getEngine();
            engine.setOnError(event -> System.out.println("WebView error: " + event.getMessage()));

            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaCallback", new ReCaptchaCallback());
                } else if (newState == Worker.State.FAILED) {
                    System.out.println("WebView failed to load: " + engine.getLoadWorker().getException());
                }
            });

            engine.load("http://localhost:8085/recaptcha");
            System.out.println("Loading reCAPTCHA page at http://localhost:8085/recaptcha");

        } catch (IOException e) {
            System.err.println("Failed to start reCAPTCHA server: " + e.getMessage());
            Platform.runLater(() -> showError("Failed to initialize reCAPTCHA: " + e.getMessage()));
        }
    }

    public class ReCaptchaCallback {
        public void onCaptchaSuccess(String response) {
            System.out.println("reCAPTCHA response: " + response); // Debug log
            recaptchaResponse = response;
            Platform.runLater(() -> {
                recaptchaStatusLabel.setText("reCAPTCHA verified!");
                recaptchaStatusLabel.setStyle("-fx-text-fill: green;");
                recaptchaVerified.set(true);
            });
        }

        public void onCaptchaExpired() {
            System.out.println("reCAPTCHA expired"); // Debug log
            recaptchaResponse = "";
            Platform.runLater(() -> {
                recaptchaStatusLabel.setText("reCAPTCHA expired. Please verify again.");
                recaptchaStatusLabel.setStyle("-fx-text-fill: red;");
                recaptchaVerified.set(false);
            });
        }
    }
//
//    public class ReCaptchaCallback {
//        public void onCaptchaSuccess(String response) {
//            recaptchaResponse = response;
//            Platform.runLater(() -> {
//                recaptchaStatusLabel.setText("reCAPTCHA verified!");
//                recaptchaStatusLabel.setStyle("-fx-text-fill: green;");
//                recaptchaVerified.set(true);
//                System.out.println("reCAPTCHA verified successfully. Response: " + response);
//            });
//        }
//
//        public void onCaptchaExpired() {
//            recaptchaResponse = "";
//            Platform.runLater(() -> {
//                recaptchaStatusLabel.setText("reCAPTCHA expired. Please verify again.");
//                recaptchaStatusLabel.setStyle("-fx-text-fill: red;");
//                recaptchaVerified.set(false);
//                System.out.println("reCAPTCHA expired");
//            });
//        }
//    }

    private boolean verifyRecaptchaWithServer() {
        if (recaptchaResponse.isEmpty()) {
            System.out.println("reCAPTCHA response is empty");
            return false;
        }

        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("secret", RECAPTCHA_SECRET_KEY)
                    .add("response", recaptchaResponse)
                    .build();

            Request request = new Request.Builder()
                    .url("https://www.google.com/recaptcha/api/siteverify")
                    .post(formBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                System.out.println("reCAPTCHA verification response: " + responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);
                boolean success = jsonResponse.getBoolean("success");

                if (!success && jsonResponse.has("error-codes")) {
                    System.out.println("reCAPTCHA error codes: " + jsonResponse.getJSONArray("error-codes").toString());
                }

                return success;
            }
        } catch (Exception e) {
            System.err.println("Error verifying reCAPTCHA: " + e.getMessage());
            Platform.runLater(() -> showError("Error verifying reCAPTCHA: " + e.getMessage()));
            return false;
        }
    }

    @FXML
    private void handleGoogleSignup() {
        try {
            stateToken = String.valueOf(new Random().nextLong());

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
                    Class<?> desktopClass = Class.forName("java.awt.Desktop");
                    Object desktop = desktopClass.getMethod("getDesktop").invoke(null);
                    desktopClass.getMethod("browse", URI.class).invoke(desktop, new URI(authUrl));
                } catch (Exception e) {
                    throw new IOException("Could not open browser: " + e.getMessage());
                }
            }

            showInfo("Please sign up with Google in your browser. You will be redirected back to the application.");

        } catch (IOException e) {
            showError("Error opening browser: " + e.getMessage());
            e.printStackTrace();
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
        // Implementation commented out as per original code
    }

    public void prefillGoogleUserInfo(String email, String firstName, String lastName) {
        Platform.runLater(() -> {
            emailField.setText(email);
            nomField.setText(firstName);
            prenomField.setText(lastName);

            emailField.setDisable(true);

            if (isGoogleSignup) {
                passwordField.setVisible(false);
                confirmPasswordField.setVisible(false);
                showInfo("Please complete your profile information");
            }
        });
    }

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

                dynamicFieldsContainer.getChildren().addAll(specialiteField, experienceField, diplomeField);
                break;

            case "ROLE_PATIENT":
                ageField = new TextField();
                genderComboBox = new ComboBox<>();
                bloodTypeField = new TextField();

                ageField.setPromptText("Age");
                genderComboBox.setPromptText("Gender");
                genderComboBox.getItems().addAll("Male", "Female");
                bloodTypeField.setPromptText("Blood Type");

                dynamicFieldsContainer.getChildren().addAll(ageField, genderComboBox, bloodTypeField);
                break;

            case "ROLE_DONATEUR":
                donateurTypeField = new TextField();
                donateurTypeField.setPromptText("Donateur Type");
                dynamicFieldsContainer.getChildren().add(donateurTypeField);
                break;
        }

        dynamicFieldsContainer.getChildren().forEach(node -> {
            if (node instanceof TextField || node instanceof ComboBox) {
                node.setStyle("-fx-pref-width: 300px; -fx-pref-height: 40px");
            }
        });
    }

    @FXML
    private void handleSignup() throws SQLException {
        if (!validateFields()) return;

        // Force re-verification with the current response
//        if (!verifyRecaptchaWithServer()) {
//            showError("reCAPTCHA verification failed. Please try again or complete the reCAPTCHA.");
//            recaptchaVerified.set(true);
//            recaptchaStatusLabel.setText("Please complete the reCAPTCHA");
//            recaptchaStatusLabel.setStyle("-fx-text-fill: red;");
//            return;
//        }

        recaptchaVerified.set(true);
        recaptchaStatusLabel.setText("reCAPTCHA verified!");
        recaptchaStatusLabel.setStyle("-fx-text-fill: green;");

        if (!isGoogleSignup && UserDAO.isEmailExists(emailField.getText())) {
            showError("Email already registered");
            return;
        }

        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format");
            return;
        }

        try {
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
        showError(message);
    }

    private boolean validateFields() {
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
                        0,
                        nomField.getText(),
                        prenomField.getText(),
                        emailField.getText(),
                        telField.getText(),
                        adresseField.getText(),
                        hashedPassword,
                        "",
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

                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);

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
                        Platform.runLater(() -> errorLabel.setVisible(false));
                    }
                },
                5000
        );
    }
}