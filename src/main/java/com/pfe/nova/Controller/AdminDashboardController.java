package com.pfe.nova.Controller;

import com.pfe.nova.configuration.PostDAO;
import com.pfe.nova.configuration.RapportDAO;
import com.pfe.nova.models.*;
import com.pfe.nova.utils.Session;
import com.pfe.nova.configuration.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.Optional;
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
    @FXML private Button postsManagementBtn;
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
    private void showPendingPosts() {
        try {
            // Create a tab content for pending posts
            VBox pendingPostsContent = new VBox(10);
            pendingPostsContent.setPadding(new Insets(20));
            pendingPostsContent.setStyle("-fx-background-color: white;");

            // Add header
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            Label titleLabel = new Label("Pending Posts");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button refreshButton = new Button("Refresh");
            refreshButton.setOnAction(e -> loadPendingPostsContent(pendingPostsContent));
            header.getChildren().addAll(titleLabel, spacer, refreshButton);

            // Add progress indicator
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setVisible(false);

            // Add scroll pane for posts
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);

            // Create container for posts
            VBox postsContainer = new VBox(15);
            scrollPane.setContent(postsContainer);

            // Add all to main container
            pendingPostsContent.getChildren().addAll(header, progressIndicator, scrollPane);

            // Get existing tabs
            Tab pendingPostsTab = null;

            // Check if tab already exists
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Pending Posts")) {
                    pendingPostsTab = tab;
                    break;
                }
            }

            // Create new tab if it doesn't exist
            if (pendingPostsTab == null) {
                pendingPostsTab = new Tab("Pending Posts");
                pendingPostsTab.setContent(pendingPostsContent);
                pendingPostsTab.setClosable(true);
                mainTabPane.getTabs().add(pendingPostsTab);
            } else {
                // Update existing tab content
                pendingPostsTab.setContent(pendingPostsContent);
            }

            // Select the tab
            mainTabPane.getSelectionModel().select(pendingPostsTab);

            // Load pending posts
            loadPendingPostsContent(pendingPostsContent);

        } catch (Exception e) {
            showError("Error loading pending posts: " + e.getMessage());
            e.printStackTrace();
        }
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
    private void loadPendingPostsContent(VBox container) {
        // Find the progress indicator and posts container
        ProgressIndicator progressIndicator = null;
        ScrollPane scrollPane = null;

        for (javafx.scene.Node node : container.getChildren()) {
            if (node instanceof ProgressIndicator) {
                progressIndicator = (ProgressIndicator) node;
            } else if (node instanceof ScrollPane) {
                scrollPane = (ScrollPane) node;
            }
        }

        if (progressIndicator == null || scrollPane == null) {
            showError("UI components not found");
            return;
        }

        // Show loading indicator
        progressIndicator.setVisible(true);

        // Get posts container
        VBox postsContainer = (VBox) scrollPane.getContent();
        postsContainer.getChildren().clear();

        // Load posts in background thread
        ProgressIndicator finalProgressIndicator = progressIndicator;
        ProgressIndicator finalProgressIndicator1 = progressIndicator;
        Thread loadThread = new Thread(() -> {
            try {
                // Get pending posts from database
                List<Post> pendingPosts = PostDAO.findByStatus("pending");

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    try {
                        // Hide loading indicator
                        finalProgressIndicator.setVisible(false);

                        if (pendingPosts.isEmpty()) {
                            Label noPostsLabel = new Label("No pending posts to display");
                            noPostsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
                            postsContainer.getChildren().add(noPostsLabel);
                        } else {
                            Label countLabel = new Label("Found " + pendingPosts.size() + " pending posts");
                            countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 0 0 10 0;");
                            postsContainer.getChildren().add(countLabel);

                            // Add each post to the container
                            for (Post post : pendingPosts) {
                                VBox postView = createPendingPostView(post);
                                postsContainer.getChildren().add(postView);
                            }
                        }
                    } catch (Exception e) {
                        finalProgressIndicator.setVisible(false);
                        showError("Error displaying posts: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    finalProgressIndicator1.setVisible(false);
                    showError("Error loading posts: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
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
    private VBox createPendingPostView(Post post) {
        VBox postBox = new VBox(10);
        postBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: white; -fx-background-radius: 5;");

        // User info section
        HBox userInfo = new HBox(10);
        userInfo.setAlignment(Pos.CENTER_LEFT);

        Label userLabel = new Label(post.isAnonymous() ? "Anonymous User" : post.getUser().getNom() + " " + post.getUser().getPrenom());
        userLabel.setStyle("-fx-font-weight: bold;");

        // Use a simple "Pending" label instead of trying to access a date
        Label dateLabel = new Label("Pending");
        if (post.getPublishDate() != null) {
            dateLabel.setText(post.getPublishDate().toString());
        }
        dateLabel.setStyle("-fx-text-fill: #7f8c8d;");

        Label statusLabel = new Label("PENDING");
        statusLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        userInfo.getChildren().addAll(userLabel, dateLabel, spacer, statusLabel);

        // Post content - using content instead of subject since there's no getSubject() method
        Label titleLabel = new Label("Post #" + post.getId());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px;");

        // Category label if available
        if (post.getCategory() != null && !post.getCategory().isEmpty()) {
            Label categoryLabel = new Label(post.getCategory());
            categoryLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3;");
            postBox.getChildren().add(categoryLabel);
        }

        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

        Button approveButton = new Button("Approve");
        approveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        approveButton.setOnAction(e -> handleApprovePost(post));

        Button rejectButton = new Button("Reject");
        rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        rejectButton.setOnAction(e -> handleRejectPost(post));

        actionButtons.getChildren().addAll(approveButton, rejectButton);

        // Add all components to post box
        postBox.getChildren().addAll(userInfo, titleLabel, contentLabel, actionButtons);

        return postBox;
    }
    private void handleApprovePost(Post post) {
        try {
            // Update post status to approved
            post.setStatus("approved");
            // Use the correct method from PostDAO
            PostDAO.save(post); // Changed from updatePost to save

            // Find and refresh the pending posts tab
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Pending Posts")) {
                    VBox content = (VBox) tab.getContent();
                    loadPendingPostsContent(content);
                    break;
                }
            }

            showInfo("Post approved successfully");
        } catch (Exception e) {
            showError("Error approving post: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle rejecting a post
     */
    private void handleRejectPost(Post post) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Reject Post");
        confirmDialog.setHeaderText("Are you sure you want to reject this post?");
        confirmDialog.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Update post status to rejected
                post.setStatus("rejected");
                // Use the correct method from PostDAO
                PostDAO.save(post); // Changed from updatePost to save

                // Find and refresh the pending posts tab
                for (Tab tab : mainTabPane.getTabs()) {
                    if (tab.getText().equals("Pending Posts")) {
                        VBox content = (VBox) tab.getContent();
                        loadPendingPostsContent(content);
                        break;
                    }
                }

                showInfo("Post rejected successfully");
            } catch (Exception e) {
                showError("Error rejecting post: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void showReportedComments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/reported-comments.fxml"));
            Parent reportedCommentsView = loader.load();

            // Get existing tabs
            Tab reportedCommentsTab = null;

            // Check if tab already exists
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Reported Comments")) {
                    reportedCommentsTab = tab;
                    break;
                }
            }

            // Create new tab if it doesn't exist
            if (reportedCommentsTab == null) {
                reportedCommentsTab = new Tab("Reported Comments");
                reportedCommentsTab.setContent(reportedCommentsView);
                reportedCommentsTab.setClosable(true);
                mainTabPane.getTabs().add(reportedCommentsTab);
            }

            // Select the tab
            mainTabPane.getSelectionModel().select(reportedCommentsTab);

        } catch (IOException e) {
            showError("Error loading reported comments: " + e.getMessage());
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
    }
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}