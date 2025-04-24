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

import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;
import java.sql.SQLException;
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
    private Button postsManagementBtn; // Add this field for the posts management button

    @FXML
    public void initialize() {
        setupUI();
        loadUsersData();  // Keep only this line

        User sessionUser = Session.getInstance().getUtilisateurConnecte();
        if (sessionUser != null) {
            sessionTestLabel.setText("Session User: " + sessionUser.getEmail());
        } else {
            sessionTestLabel.setText("No user in session.");
        }
    }


    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterRole;
    private User adminUser;


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
            VBox postsContent = new VBox(10);
            postsContent.setPadding(new Insets(20));
            postsContent.setStyle("-fx-background-color: white;");

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            Label titleLabel = new Label("Posts Management");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button refreshButton = new Button("Refresh");
            refreshButton.setOnAction(e -> loadAllPostsContent(postsContent, "ALL"));
            header.getChildren().addAll(titleLabel, spacer, refreshButton);

            // Add filter buttons
            HBox filterButtons = new HBox(10);
            filterButtons.setAlignment(Pos.CENTER_LEFT);
            filterButtons.setPadding(new Insets(10, 0, 20, 0));
            
            Button allButton = new Button("All Posts");
            allButton.getStyleClass().add("filter-button");
            allButton.setOnAction(e -> loadAllPostsContent(postsContent, "ALL"));
            
            Button pendingButton = new Button("Pending");
            pendingButton.getStyleClass().add("filter-button");
            pendingButton.setOnAction(e -> loadAllPostsContent(postsContent, "pending"));
            
            Button approvedButton = new Button("Approved");
            approvedButton.getStyleClass().add("filter-button");
            approvedButton.setOnAction(e -> loadAllPostsContent(postsContent, "approved"));
            
            Button refusedButton = new Button("Refused");
            refusedButton.getStyleClass().add("filter-button");
            refusedButton.setOnAction(e -> loadAllPostsContent(postsContent, "refused"));
            
            filterButtons.getChildren().addAll(allButton, pendingButton, approvedButton, refusedButton);

            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setVisible(false);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);

            FlowPane postsContainer = new FlowPane(15, 15);
            postsContainer.setPadding(new Insets(10));
            scrollPane.setContent(postsContainer);

            postsContent.getChildren().addAll(header, filterButtons, progressIndicator, scrollPane);

            Tab postsTab = null;

            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Posts Management")) {
                    postsTab = tab;
                    break;
                }
            }

            // Create new tab if it doesn't exist
            if (postsTab == null) {
                postsTab = new Tab("Posts Management");
                postsTab.setContent(postsContent);
                postsTab.setClosable(true);
                mainTabPane.getTabs().add(postsTab);
            } else {
                // Update existing tab content
                postsTab.setContent(postsContent);
            }

            // Select the tab
            mainTabPane.getSelectionModel().select(postsTab);

            // Load all posts initially
            loadAllPostsContent(postsContent, "ALL");

        } catch (Exception e) {
            showError("Error loading posts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all posts with filtering capability
     * @param container The container to display posts in
     * @param filter The filter to apply (ALL, pending, approved, refused)
     */
    private void loadAllPostsContent(VBox container, String filter) {
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
    
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
    
        if (scrollPane != null) {
            // Use FlowPane with proper spacing for card layout
            FlowPane postsContainer = new FlowPane();
            postsContainer.setHgap(30); // Horizontal gap between cards
            postsContainer.setVgap(30); // Vertical gap between cards
            postsContainer.setPadding(new Insets(25));
            postsContainer.setAlignment(Pos.CENTER);
            scrollPane.setContent(postsContainer);
    
            try {
                List<Post> allPosts;
                
                // Get posts based on filter
                if ("ALL".equals(filter)) {
                    allPosts = PostDAO.findAll();
                } else {
                    allPosts = PostDAO.findByStatus(filter);
                }
                
                if (allPosts.isEmpty()) {
                    Label noPostsLabel = new Label("No posts found");
                    noPostsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
                    postsContainer.getChildren().add(noPostsLabel);
                } else {
                    for (Post post : allPosts) {
                        VBox postCard = createPostCard(post);
                        postsContainer.getChildren().add(postCard);
                    }
                }
            } catch (SQLException e) {
                showError("Error loading posts: " + e.getMessage());
            } finally {
                if (progressIndicator != null) {
                    progressIndicator.setVisible(false);
                }
            }
        }
    }
    
    private VBox createPostCard(Post post) {
        VBox postBox = new VBox(10);
        postBox.getStyleClass().add("post-card");
        postBox.setPadding(new Insets(20));
        postBox.setPrefWidth(300);
        postBox.setMaxWidth(300);
        postBox.setMinHeight(250);
        
        // Add card styling
        postBox.setStyle("-fx-background-color: white; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-radius: 10;");
        
        // Status indicator with rounded corners
        Label statusLabel = new Label(post.getStatus().toUpperCase());
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        statusLabel.setStyle("-fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        // Style based on status
        switch (post.getStatus()) {
            case "pending":
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #f39c12; -fx-text-fill: white;");
                break;
            case "approved":
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                break;
            case "refused":
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
                break;
        }
        
        // Post header with user info
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
    
        Label userLabel = new Label(post.getUser().getNom() + " " + post.getUser().getPrenom());
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    
        Label categoryLabel = new Label(post.getCategory());
        categoryLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                          "-fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 12px;");
    
        header.getChildren().addAll(userLabel, categoryLabel, statusLabel);
    
        // Post content with proper wrapping
        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setMaxHeight(80);
        contentLabel.setStyle("-fx-font-size: 14px;");
        
        // Truncate long content
        if (post.getContent().length() > 100) {
            contentLabel.setText(post.getContent().substring(0, 100) + "...");
        }
    
        // Add images if any (just show one thumbnail)
        ImageView imageView = null;
        if (!post.getImageUrls().isEmpty()) {
            try {
                String imagePath = post.getImageUrls().get(0);
                Image image;
                if (imagePath.startsWith("http")) {
                    image = new Image(imagePath);
                } else {
                    image = new Image(new File(imagePath).toURI().toString());
                }
    
                imageView = new ImageView(image);
                imageView.setFitHeight(120);
                imageView.setFitWidth(260);
                imageView.setPreserveRatio(true);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    
        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));
    
        Button viewButton = new Button("View");
        viewButton.getStyleClass().add("view-button");
        viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
        viewButton.setOnAction(e -> openPostDetails(post));
    
        Button approveButton = new Button("Approve");
        approveButton.getStyleClass().add("approve-button");
        approveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 5;");
        approveButton.setOnAction(e -> handleApprovePost(post));
        
        Button rejectButton = new Button("Reject");
        rejectButton.getStyleClass().add("reject-button");
        rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
        rejectButton.setOnAction(e -> handleRejectPost(post));
        
        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
        deleteButton.setOnAction(e -> handleDeletePost(post));
    
        // Only show appropriate buttons based on status
        if ("pending".equals(post.getStatus())) {
            actionButtons.getChildren().addAll(viewButton, approveButton, rejectButton);
        } else {
            actionButtons.getChildren().addAll(viewButton, deleteButton);
        }
    
        // Add all components to post box
        postBox.getChildren().add(header);
        postBox.getChildren().add(new Separator());
        postBox.getChildren().add(contentLabel);
    
        // Add image if available
        if (imageView != null) {
            HBox imageContainer = new HBox();
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.getChildren().add(imageView);
            postBox.getChildren().add(imageContainer);
        }
    
        // Add a spacer to push buttons to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        postBox.getChildren().add(spacer);
        
        // Add separator before buttons
        postBox.getChildren().add(new Separator());
        postBox.getChildren().add(actionButtons);
    
        return postBox;
    }
    
    private void openPostDetails(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/publication-details.fxml"));
            Parent root = loader.load();

            PublicationDetailsController controller = loader.getController();
            controller.setPost(post);

            Stage stage = new Stage();
            stage.setTitle("Publication Details");
            
            // Set a reasonable size for the publication details window
            Scene scene = new Scene(root, 800, 700);
            stage.setScene(scene);
            
            // Set minimum size constraints
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            
            stage.show();
        } catch (IOException e) {
            showError("Error opening publication details: " + e.getMessage());
        }
    }
    
    private void handleApprovePost(Post post) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Approve Post");
        confirmation.setHeaderText("Approve Post");
        confirmation.setContentText("Are you sure you want to approve this post?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                post.setStatus("approved");
                PostDAO.updateStatus(post.getId(), "approved");

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Post has been approved successfully!");
                success.showAndWait();

                // Refresh the posts list
                showPendingPosts();
            } catch (SQLException e) {
                showError("Error approving post: " + e.getMessage());
            }
        }
    }
    
    private void handleRejectPost(Post post) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Reject Post");
        confirmation.setHeaderText("Reject Post");
        confirmation.setContentText("Are you sure you want to reject this post?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                post.setStatus("refused");
                PostDAO.updateStatus(post.getId(), "refused");

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Post has been rejected successfully!");
                success.showAndWait();

                // Refresh the posts list
                showPendingPosts();
            } catch (SQLException e) {
                showError("Error rejecting post: " + e.getMessage());
            }
        }
    }
    
    private void handleDeletePost(Post post) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Post");
        confirmation.setHeaderText("Delete Post");
        confirmation.setContentText("Are you sure you want to delete this post? This action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                PostDAO.delete(post.getId());

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Post has been deleted successfully!");
                success.showAndWait();

                // Refresh the posts list
                showPendingPosts();
            } catch (SQLException e) {
                showError("Error deleting post: " + e.getMessage());
            }
        }
    }

    @FXML
    private void showReportedComments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/reported-comments.fxml"));
            Parent reportedCommentsContent = loader.load();
            
            Tab reportedCommentsTab = null;
            
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Reported Comments")) {
                    reportedCommentsTab = tab;
                    break;
                }
            }
            
            // Create new tab if it doesn't exist
            if (reportedCommentsTab == null) {
                reportedCommentsTab = new Tab("Reported Comments");
                reportedCommentsTab.setContent(reportedCommentsContent);
                reportedCommentsTab.setClosable(true);
                mainTabPane.getTabs().add(reportedCommentsTab);
            } else {
                // Update existing tab content
                reportedCommentsTab.setContent(reportedCommentsContent);
            }
            
            // Select the tab
            mainTabPane.getSelectionModel().select(reportedCommentsTab);
            
        } catch (Exception e) {
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
    private void showReports() {
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
            stage.setOnHiding(event -> loadUsersData());

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error opening edit window: " + e.getMessage());
        } catch (Exception e) {
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
    private void showProfile() {
        try {
            // Check if the Profile tab already exists
            Tab profileTab = null;
            for (Tab tab : mainTabPane.getTabs()) {
                if ("Profile".equals(tab.getText())) {
                    profileTab = tab;
                    break;
                }
            }

            if (profileTab == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/profile.fxml"));
                Parent profileRoot = loader.load();

                // Pass the current admin user to the ProfileController
                ProfileController profileController = loader.getController();
                // Use the session user if available, otherwise fallback to adminUser
                User user = Session.getInstance().getUtilisateurConnecte();
                if (user == null) user = adminUser;
                profileController.initData(user);

                profileTab = new Tab("Profile", profileRoot);
                profileTab.setClosable(true);
                mainTabPane.getTabs().add(profileTab);
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
}



