package com.pfe.nova.Controller;

import com.pfe.nova.configuration.PostDAO;
import com.pfe.nova.configuration.UserDAO;
import com.pfe.nova.models.Post;
import com.pfe.nova.models.User;
import com.pfe.nova.services.EmailPostService;
import com.pfe.nova.services.EmailPostTemplateService;
import com.pfe.nova.utils.Session;
import javafx.application.Platform;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javafx.concurrent.Task;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private StackPane contentArea;
    @FXML private TabPane mainTabPane;
    @FXML private GridPane usersGrid;
    @FXML private Label sidebarProfileName;
    @FXML private Label sidebarProfileEmail;
    @FXML private ImageView sidebarProfileImage;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> actionsColumn;
    @FXML private Label sessionTestLabel;
    @FXML private VBox pharmacieSubMenu;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterRole;

    private User adminUser;

    @FXML
    public void initialize() {
        setupUI();
        loadUsersData();
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
                    editButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        handleEditUser(user);
                    });
                    deleteButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        if (confirmDelete(user)) {
                            UserDAO.deleteUser(user.getId());
                            loadUsersData();
                        }
                    });
                    editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttons);
                }
            });
        } catch (Exception e) {
            System.err.println("Error setting up table columns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void initData(User user) {
        if (!"ROLE_ADMIN".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            System.err.println("Non-admin user attempted to access admin dashboard");
            handleLogout();
            return;
        }
        this.adminUser = user;
        welcomeLabel.setText("Welcome, " + user.getNom() + " " + user.getPrenom());
        sidebarProfileName.setText(user.getNom() + " " + user.getPrenom());
        sidebarProfileEmail.setText(user.getEmail());
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
        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        filterRole.valueProperty().addListener((observable, oldValue, newValue) -> handleSearch());
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().logout();
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
        usersGrid.getChildren().clear();
        int col = 0;
        int row = 0;
        for (User user : filteredUsers) {
            createUserCard(user, row, col);
            col++;
            if (col == 3) {
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

            if (postsTab == null) {
                postsTab = new Tab("Posts Management");
                postsTab.setContent(postsContent);
                postsTab.setClosable(true);
                mainTabPane.getTabs().add(postsTab);
            } else {
                postsTab.setContent(postsContent);
            }

            mainTabPane.getSelectionModel().select(postsTab);
            loadAllPostsContent(postsContent, "ALL");

        } catch (Exception e) {
            showError("Error loading posts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAllPostsContent(VBox container, String filter) {
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
            FlowPane postsContainer = new FlowPane();
            postsContainer.setHgap(30);
            postsContainer.setVgap(30);
            postsContainer.setPadding(new Insets(25));
            postsContainer.setAlignment(Pos.CENTER);
            scrollPane.setContent(postsContainer);
            try {
                List<Post> allPosts;
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
        postBox.setStyle("-fx-background-color: white; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-background-radius: 10; -fx-border-radius: 10;");
        Label statusLabel = new Label(post.getStatus().toUpperCase());
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        statusLabel.setStyle("-fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;");
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
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label userLabel = new Label(post.getUser().getNom() + " " + post.getUser().getPrenom());
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label categoryLabel = new Label(post.getCategory());
        categoryLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 12px;");
        header.getChildren().addAll(userLabel, categoryLabel, statusLabel);
        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setMaxHeight(80);
        contentLabel.setStyle("-fx-font-size: 14px;");
        if (post.getContent().length() > 100) {
            contentLabel.setText(post.getContent().substring(0, 100) + "...");
        }
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
        if ("pending".equals(post.getStatus())) {
            actionButtons.getChildren().addAll(viewButton, approveButton, rejectButton);
        } else {
            actionButtons.getChildren().addAll(viewButton, deleteButton);
        }
        postBox.getChildren().add(header);
        postBox.getChildren().add(new Separator());
        postBox.getChildren().add(contentLabel);
        if (imageView != null) {
            HBox imageContainer = new HBox();
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.getChildren().add(imageView);
            postBox.getChildren().add(imageContainer);
        }
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        postBox.getChildren().add(spacer);
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
            Scene scene = new Scene(root, 800, 700);
            stage.setScene(scene);
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            stage.show();
        } catch (IOException e) {
            showError("Error opening publication details: " + e.getMessage());
        }
    }





    private void handleApprovePost(Post post) {
        System.out.println("handleApprovePost called on thread: " + Thread.currentThread().getName());
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Approve Post");
        confirmation.setHeaderText("Approve Post");
        confirmation.setContentText("Are you sure you want to approve this post?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                post.setStatus("approved");
                PostDAO.updateStatus(post.getId(), "approved");

                User postOwner = post.getUser();
                if (postOwner != null && postOwner.getEmail() != null) {
                    Task<Void> emailTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            // Use the correct App Password
                            EmailPostService emailPostService = new EmailPostService("benalibenalirania123@gmail.com", "jwzu mmvp vsol qwuh");
                            String subject = "Votre publication a été approuvée";
                            String htmlContent = EmailPostTemplateService.getPostApprovalTemplate(postOwner, post);
                            emailPostService.sendHtmlEmail(postOwner.getEmail(), subject, htmlContent);
                            return null;
                        }
                        
                        @Override
                        protected void succeeded() {
                            System.out.println("Email de notification envoyé à " + postOwner.getEmail());
                        }

                        @Override
                        protected void failed() {
                            Throwable exception = getException();
                            exception.printStackTrace();
                            System.err.println("Erreur lors de l'envoi de l'email: " + exception.getMessage());
                            Platform.runLater(() -> showError("Failed to send email notification: " + exception.getMessage()));
                        }
                    };
                    new Thread(emailTask).start();
                }

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Post has been approved successfully!");
                success.showAndWait();

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
            if (reportedCommentsTab == null) {
                reportedCommentsTab = new Tab("Reported Comments");
                reportedCommentsTab.setContent(reportedCommentsContent);
                reportedCommentsTab.setClosable(true);
                mainTabPane.getTabs().add(reportedCommentsTab);
            } else {
                reportedCommentsTab.setContent(reportedCommentsContent);
            }
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
    private void showPostsStatistics() {
        try {
            String tabId = "postsStatisticsTab";
            Tab postsStatisticsTab = findTab(tabId);
            if (postsStatisticsTab == null) {
                postsStatisticsTab = new Tab("Publications Statistics");
                postsStatisticsTab.setId(tabId);
                postsStatisticsTab.setClosable(false);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-statistics.fxml"));
                Parent content = loader.load();
                postsStatisticsTab.setContent(content);
                mainTabPane.getTabs().add(postsStatisticsTab);
            }
            mainTabPane.getSelectionModel().select(postsStatisticsTab);
        } catch (IOException e) {
            showError("Error loading posts statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Tab findTab(String id) {
        for (Tab tab : mainTabPane.getTabs()) {
            if (id.equals(tab.getId())) {
                return tab;
            }
        }
        return null;
    }

    @FXML
    private void showProfile() {
        try {
            Tab profileTab = null;
            ProfileController profileController = null;
            for (Tab tab : mainTabPane.getTabs()) {
                if ("Profile".equals(tab.getText())) {
                    profileTab = tab;
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
                profileController = loader.getController();
                profileRoot.setUserData(profileController);
                profileTab = new Tab("Profile", profileRoot);
                profileTab.setClosable(true);
                mainTabPane.getTabs().add(profileTab);
            }
            if (profileController != null) {
                User user = Session.getInstance().getUtilisateurConnecte();
                if (user == null) user = adminUser;
                User refreshedUser = UserDAO.getUserById(user.getId());
                profileController.initData(refreshedUser != null ? refreshedUser : user);
            }
            mainTabPane.getSelectionModel().select(profileTab);
        } catch (IOException e) {
            showError("Error loading profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createUserCard(User user, int row, int col) {
        VBox card = new VBox(10);
        card.getStyleClass().add("user-card");
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        Label roleLabel = new Label(user.getRole());
        roleLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-padding: 5 10; -fx-background-radius: 4; -fx-font-size: 12px;");
        Label nameLabel = new Label(user.getNom() + " " + user.getPrenom());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
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
        card.getChildren().addAll(roleLabel, nameLabel, emailLabel, actions);
        card.setOnMouseEntered(e ->
                card.setStyle(card.getStyle() + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 14, 0, 0, 0);"));
        card.setOnMouseExited(e ->
                card.setStyle(card.getStyle().replace("rgba(0,0,0,0.2), 14", "rgba(0,0,0,0.1), 10")));
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
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }

    @FXML
    private void handleEditUser(User user) {
        try {
            User fullUser = UserDAO.getUserById(user.getId());
            if (fullUser == null) {
                showError("Could not load user data");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/edituser.fxml"));
            Parent root = loader.load();
            EditUserController editController = loader.getController();
            editController.initData(fullUser);
            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setOnHiding(event -> {
                loadUsersData();
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
            showError("Error opening edit window: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateSidebarInfo(User user) {
        if (user == null) return;
        sidebarProfileName.setText(user.getNom() + " " + user.getPrenom());
        sidebarProfileEmail.setText(user.getEmail());
        if (user.getPicture() != null && !user.getPicture().isEmpty()) {
            try {
                Image image = new Image(Paths.get(user.getPicture()).toUri().toString());
                sidebarProfileImage.setImage(image);
            } catch (Exception e) {
                System.err.println("Error updating profile image: " + e.getMessage());
            }
        }
        if (user.getId() == adminUser.getId()) {
            this.adminUser = user;
        }
        loadUsersData();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete " + user.getNom() + " " + user.getPrenom() + "?");
        return alert.showAndWait().map(result -> result == ButtonType.OK).orElse(false);
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/signup.fxml"));
            Parent root = loader.load();
            SignupController signupController = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Add New User");
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadUsersData());
            stage.show();
        } catch (IOException e) {
            showError("Error opening add user window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showUsersManagement() {
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
            VBox content = new VBox(20);
            content.setPadding(new Insets(20));
            content.setStyle("-fx-background-color: white;");
            HBox controls = new HBox(15);
            controls.setAlignment(Pos.CENTER_LEFT);
            searchField = new TextField();
            searchField.setPromptText("Search users...");
            searchField.setPrefWidth(300);
            filterRole = new ComboBox<>();
            filterRole.getItems().addAll("ALL", "ROLE_ADMIN", "ROLE_MEDECIN", "ROLE_PATIENT", "ROLE_DONATEUR");
            filterRole.setValue("ALL");
            Button addUserBtn = new Button("Add User");
            addUserBtn.setOnAction(this::handleAddUser);
            controls.getChildren().addAll(searchField, filterRole, addUserBtn);
            usersGrid = new GridPane();
            usersGrid.setHgap(20);
            usersGrid.setVgap(20);
            content.getChildren().addAll(controls, usersGrid);
            usersTab.setContent(content);
            mainTabPane.getTabs().add(usersTab);
        }
        mainTabPane.getSelectionModel().select(usersTab);
        loadUsersData();
    }

    @FXML
    private void showMedicationManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/medication_management.fxml"));
            Parent root = loader.load();
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
            showError("Erreur lors du chargement des statistiques des avis: " + e.getMessage());
            e.printStackTrace();
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
            showError("Erreur lors du chargement des statistiques des avis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void showSessions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/viewSessions.fxml"));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            showError("Failed to load viewSessions.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void showReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/RapportsAdmin.fxml"));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            showError("Failed to load RapportsAdmin.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void togglePharmacieMenu() {
        pharmacieSubMenu.setVisible(!pharmacieSubMenu.isVisible());
        pharmacieSubMenu.setManaged(!pharmacieSubMenu.isManaged());
    }
}