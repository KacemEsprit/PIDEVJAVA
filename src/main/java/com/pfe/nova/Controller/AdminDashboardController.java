package com.pfe.nova.Controller;

import com.pfe.nova.configuration.PostDAO;
import com.pfe.nova.models.*;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
    @FXML private Label sessionTestLabel;

    @FXML private Button postsManagementBtn; // Add this field for the posts management button

    @FXML
    public void initialize() {
        setupUI();
        setupTableColumns();
        loadUsersData();

        User sessionUser = Session.getInstance().getUtilisateurConnecte();
        if (sessionUser != null) {
            sessionTestLabel.setText("Session User: " + sessionUser.getEmail());
        } else {
            sessionTestLabel.setText("No user in session.");
        }
    }
//    @FXML
//    public void initialize() {
//        setupUI();
//        setupTableColumns();
//        loadUsersData(); // Add this line to load data when initializing
//    }

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

    }
    

    
    @FXML
    private void showUsersManagement() {
        mainTabPane.getSelectionModel().select(0);
    }
    

    @FXML
    private void showPendingPosts() {
        try {
            VBox pendingPostsContent = new VBox(10);
            pendingPostsContent.setPadding(new Insets(20));
            pendingPostsContent.setStyle("-fx-background-color: white;");

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            Label titleLabel = new Label("Pending Posts");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button refreshButton = new Button("Refresh");
            refreshButton.setOnAction(e -> loadPendingPostsContent(pendingPostsContent));
            header.getChildren().addAll(titleLabel, spacer, refreshButton);

            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setVisible(false);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);

            VBox postsContainer = new VBox(15);
            scrollPane.setContent(postsContainer);

            pendingPostsContent.getChildren().addAll(header, progressIndicator, scrollPane);

            Tab pendingPostsTab = null;

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

    /**
     * Load pending posts content
     */
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

    /**
     * Create a view for a pending post
     */
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

    /**
     * Handle approving a post
     */
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

    /**
     * Show reported comments tab
     */
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

    @FXML
    private void showStatistics() {
        // TODO: Implement statistics view
    }
    
    @FXML
    private void showReports() {
    }
    
    @FXML
    private void showSettings() {
    }
    
    private void loadUsersData() {
        List<User> users = UserDAO.getAllUsers();
        usersTable.getItems().clear();
        usersTable.getItems().addAll(users);
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Display an error message to the user
     */
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/edituser.fxml"));
            Parent root = loader.load();
            EditUserController editController = loader.getController();
            editController.initData(fullUser); // Pass the full user object

            Stage stage = new Stage();
            stage.setTitle("Edit User - " + fullUser.getRole());
            stage.setScene(new Scene(root));

            // Add a listener to refresh the table when the edit window is closed
            stage.setOnHiding(event -> loadUsersData());

            stage.show();

        } catch (IOException e) {
            showError("Error opening edit window: " + e.getMessage());
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
    }


    // Make sure this is the last method in the class and the class has a proper closing brace
}