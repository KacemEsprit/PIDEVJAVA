package com.pfe.nova.Controller;

import com.pfe.nova.configuration.PostDAO;
import com.pfe.nova.configuration.UserDAO;
import com.pfe.nova.models.Post;
import com.pfe.nova.models.User;
import com.pfe.nova.utils.Session;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private StackPane contentArea;
    @FXML
    private TabPane mainTabPane;
    // Replace TableView with GridPane
    @FXML
    private GridPane usersGrid;
    @FXML
    private Label sidebarProfileName;
    @FXML 
    private Label sidebarProfileEmail;
    @FXML
    private Label logoLabel;
    @FXML
    private ImageView sidebarProfileImage;  // Add this line
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> actionsColumn;
    @FXML
    private Label sessionTestLabel;
    @FXML
    private VBox pharmacieSubMenu;
    @FXML
    private Button postsManagementBtn; // Add this field for the posts management button


    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterRole;

    private User adminUser;


    @FXML
    private GridPane reportsGridPane;
    @FXML
    public void initialize() {
        setupUI();
        loadUsersData();  // Keep only this line
        setupTableColumns();
        User sessionUser = Session.getInstance().getUtilisateurConnecte();
        if (sessionUser != null) {
            sessionTestLabel.setText("Session User: " + sessionUser.getEmail());
        } else {
            sessionTestLabel.setText("No user in session.");
        }
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
        if (!"ROLE_ADMIN".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            // Redirect non-admin users
            System.err.println("Non-admin user attempted to access admin dashboard");
            handleLogout();
            return;
        }

        this.adminUser = user;
        
        welcomeLabel.setText("Welcome, " + user.getNom() + " " + user.getPrenom());
        sidebarProfileName.setText(user.getNom() + " " + user.getPrenom());
        sidebarProfileEmail.setText(user.getEmail());
        
        // Load profile image if available
        if (user.getPicture() != null && !user.getPicture().isEmpty()) {
            try {
                Image image = new Image(Paths.get(user.getPicture()).toUri().toString());
                sidebarProfileImage.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
            }
        }

        loadUsersData();
    }

    private void setupUI() {
        filterRole.getItems().addAll("ALL", "ROLE_ADMIN", "ROLE_MEDECIN", "ROLE_PATIENT", "ROLE_DONATEUR");
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

        List<User> allUsers = UserDAO.getAllUsers();
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                            user.getNom().toLowerCase().contains(searchText) ||
                            user.getPrenom().toLowerCase().contains(searchText) ||
                            user.getEmail().toLowerCase().contains(searchText);

                    boolean matchesRole = "ALL".equals(selectedRole) ||
                            user.getRole().equals(selectedRole);

                    return matchesSearch && matchesRole;
                })
                .collect(java.util.stream.Collectors.toList());

        // Clear and reload the grid with filtered users
        usersGrid.getChildren().clear();
        int col = 0;
        int row = 0;
        for (User user : filteredUsers) {
            createUserCard(user, row, col);
            col++;
            if (col == 3) {  // 3 cards per row
                col = 0;
                row++;
            }
        }
    }


    @FXML
    private void showDashboard() {
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/statistics.fxml"));
            Parent statisticsView = loader.load();

            Tab statisticsTab = null;
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Statistics")) {
                    statisticsTab = tab;
                    break;
                }
            }

            if (statisticsTab == null) {
                statisticsTab = new Tab("Statistics");
                statisticsTab.setContent(statisticsView);
                statisticsTab.setClosable(true);
                mainTabPane.getTabs().add(statisticsTab);
            } else {
                statisticsTab.setContent(statisticsView);
            }

            mainTabPane.getSelectionModel().select(statisticsTab);

        } catch (IOException e) {
            showError("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    private void showSettings() {
    }

    private void createUserCard(User user, int row, int col) {
        VBox card = new VBox(10);
        card.getStyleClass().add("user-card");
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // User Role Badge
        Label roleLabel = new Label(user.getRole());
        roleLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-padding: 5 10; -fx-background-radius: 4; -fx-font-size: 12px;");

        // User Name
        Label nameLabel = new Label(user.getNom() + " " + user.getPrenom());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // User Email
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        // Action Buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-background-radius: 4;");
        editButton.setOnAction(e -> handleEditUser(user));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-background-radius: 4;");
        deleteButton.setOnAction(e -> {
            if (confirmDelete(user)) {
                UserDAO.deleteUser(user.getId());
                loadUsersData();
            }
        });

        actions.getChildren().addAll(editButton, deleteButton);

        // Add all elements to card
        card.getChildren().addAll(roleLabel, nameLabel, emailLabel, actions);

        // Add hover effect
        card.setOnMouseEntered(e ->
                card.setStyle(card.getStyle() + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 14, 0, 0, 0);"));
        card.setOnMouseExited(e ->
                card.setStyle(card.getStyle().replace("rgba(0,0,0,0.2), 14", "rgba(0,0,0,0.1), 10")));

        // Add card to grid
        usersGrid.add(card, col, row);
    }

    private void loadUsersData() {
        List<User> users = UserDAO.getAllUsers();
        usersGrid.getChildren().clear();

        int col = 0;
        int row = 0;
        for (User user : users) {
            createUserCard(user, row, col);
            col++;
            if (col == 3) {  // 3 cards per row
                col = 0;
                row++;
            }
        }
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

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/pfe/novaview/edituser.fxml"));
            Parent root = loader.load();

            // Get the controller and initialize it with user data
            EditUserController editController = loader.getController();
            editController.initData(fullUser);

            // Create and configure the stage
            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // Add a listener to refresh the table when the edit window is closed
            stage.setOnHiding(event -> {
                loadUsersData();
                
                // If the edited user is the current admin, update sidebar info
                if (fullUser.getId() == adminUser.getId()) {
                    User updatedUser = UserDAO.getUserById(adminUser.getId());
                    if (updatedUser != null) {
                        adminUser = updatedUser;
                        updateSidebarInfo(updatedUser);
                    }
                }
            });

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error opening edit window: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Unexpected error: " + e.getMessage());
        }
    }
    
    // Update sidebar information with updated user data
    public void updateSidebarInfo(User user) {
        if (user == null) return;
        
        // Update sidebar profile information
        sidebarProfileName.setText(user.getNom() + " " + user.getPrenom());
        sidebarProfileEmail.setText(user.getEmail());
        
        // Update profile image if available
        if (user.getPicture() != null && !user.getPicture().isEmpty()) {
            try {
                Image image = new Image(Paths.get(user.getPicture()).toUri().toString());
                sidebarProfileImage.setImage(image);
            } catch (Exception e) {
                System.err.println("Error updating profile image: " + e.getMessage());
            }
        }
        
        // If this is the admin user, update the stored reference
        if (user.getId() == adminUser.getId()) {
            this.adminUser = user;
        }
        
        // Refresh the users data if needed
        loadUsersData();
    }
    
    // Helper method to show error messages
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Helper method to show info messages
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Helper method to confirm deletion
    private boolean confirmDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete " + user.getNom() + " " + user.getPrenom() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }


    @FXML
    private void showProfile() {
        try {
            // Check if the Profile tab already exists
            Tab profileTab = null;
            ProfileController profileController = null;
            
            for (Tab tab : mainTabPane.getTabs()) {
                if ("Profile".equals(tab.getText())) {
                    profileTab = tab;
                    // Try to get the controller if the tab already exists
                    if (tab.getContent() instanceof Parent) {
                        Parent root = (Parent) tab.getContent();
                        profileController = (ProfileController) root.getUserData();
                    }
                    break;
                }
            }

            if (profileTab == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/profile.fxml"));
                Parent profileRoot = loader.load();

                // Pass the current admin user to the ProfileController
                profileController = loader.getController();
                // Store the controller in the root's user data for later access
                profileRoot.setUserData(profileController);
                
                profileTab = new Tab("Profile", profileRoot);
                profileTab.setClosable(true);
                mainTabPane.getTabs().add(profileTab);
            }

            // Always refresh the user data when showing the profile tab
            if (profileController != null) {
                // Use the session user if available, otherwise fallback to adminUser
                User user = Session.getInstance().getUtilisateurConnecte();
                if (user == null) user = adminUser;
                
                // Get the latest user data from the database
                User refreshedUser = UserDAO.getUserById(user.getId());
                if (refreshedUser != null) {
                    profileController.initData(refreshedUser);
                } else {
                    profileController.initData(user);
                }
            }

            mainTabPane.getSelectionModel().select(profileTab);
        } catch (IOException e) {
            showError("Error loading profile: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Make sure this is the last method in the class and the class has a proper closing brace
    @FXML
    private void handleAddUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/signup.fxml"));
            Parent root = loader.load();
            SignupController signupController = loader.getController();

            // Create a new stage for the signup window
            Stage stage = new Stage();
            stage.setTitle("Add New User");
            stage.setScene(new Scene(root));

            // Add a listener to refresh the users table when the signup window is closed
            stage.setOnHiding(e -> loadUsersData());

            stage.show();
        } catch (IOException e) {
            showError("Error opening add user window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showUsersManagement() {
        // Create a new tab for users management if it doesn't exist
        Tab usersTab = null;
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Users Management")) {
                usersTab = tab;
                break;
            }
        }
    
        if (usersTab == null) {
            usersTab = new Tab("Users Management");
            usersTab.setClosable(true);
    
            // Create content for users management
            VBox content = new VBox(20);
            content.setPadding(new Insets(20));
            content.setStyle("-fx-background-color: white;");
    
            // Add search and filter controls
            HBox controls = new HBox(15);
            controls.setAlignment(Pos.CENTER_LEFT);
            searchField = new TextField();
            searchField.setPromptText("Search users...");
            searchField.setPrefWidth(300);
            filterRole = new ComboBox<>();
            filterRole.getItems().addAll("ALL", "ADMIN", "MEDECIN", "PATIENT", "DONATEUR");
            filterRole.setValue("ALL");
            Button addUserBtn = new Button("Add User");
            addUserBtn.setOnAction(this::handleAddUser);
            controls.getChildren().addAll(searchField, filterRole, addUserBtn);
    
            // Add users grid
            usersGrid = new GridPane();
            usersGrid.setHgap(20);
            usersGrid.setVgap(20);
    
            // Add all components to content
            content.getChildren().addAll(controls, usersGrid);
    
            // Set the content and add the tab
            usersTab.setContent(content);
            mainTabPane.getTabs().add(usersTab);
        }
    
        // Select the users management tab
        mainTabPane.getSelectionModel().select(usersTab);

        // Load users data
        loadUsersData();
    }

    @FXML
    private void showMedicationManagement() {

        try {
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/medication_management.fxml"));
            Parent root = loader.load();

            // Vider le contenu existant et ajouter le nouveau
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            showError("Erreur lors du chargement de la gestion des médicaments : " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void showOrderConfirmation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/order_confirmation.fxml"));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            showError("Erreur lors du chargement de la confirmation des commandes: " + e.getMessage());
        }
    }
    @FXML
    private void showReviewsStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/reviews_statistics.fxml"));
            Parent reviewsView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(reviewsView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des statistiques des avis: " + e.getMessage());
        }
    }
    @FXML
    private void showToDoList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/todolist.fxml"));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des statistiques des avis: " + e.getMessage());
        }
    }




    public void showSessions() {
        try {
            // Load the RapportsAdmin.fxml view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/viewSessions.fxml"));
            Parent root = loader.load();

            // Set the loaded view into the contentArea
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load RapportsAdmin.fxml. Please check the file path.");
        }
    }
    public void showReports() {
        try {
            // Load the RapportsAdmin.fxml view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/RapportsAdmin.fxml"));
            Parent root = loader.load();

            // Set the loaded view into the contentArea
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load RapportsAdmin.fxml. Please check the file path.");
        }
    }
    @FXML
    private void togglePharmacieMenu() {
        pharmacieSubMenu.setVisible(!pharmacieSubMenu.isVisible());
        pharmacieSubMenu.setManaged(!pharmacieSubMenu.isManaged());
    }
}



