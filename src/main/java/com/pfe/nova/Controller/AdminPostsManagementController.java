package com.pfe.nova.Controller;

// Update the import for the Session class
import com.pfe.nova.configuration.Session;

import com.pfe.nova.models.Post;
import com.pfe.nova.configuration.PostDAO;
import com.pfe.nova.configuration.CommentDAO;
import com.pfe.nova.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AdminPostsManagementController {
    
    @FXML private ScrollPane contentScrollPane;
    @FXML private VBox contentContainer;
    @FXML private VBox pendingPostsContainer;
    @FXML private Label sectionTitle;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label contentTitle;
    
    private String currentView = "pending";
    
    @FXML
    public void initialize() {
        // Initialize UI components first
        if (progressIndicator == null) {
            progressIndicator = new ProgressIndicator();
        }
        if (statusLabel == null) {
            statusLabel = new Label();
        }
        
        // Check if user is admin before loading admin content
        User currentUser = Session.getUtilisateurConnecte();
        if (currentUser == null) {
            showError("No user logged in");
            return;
        }
        
        String role = currentUser.getRole().toUpperCase();
        if (!role.contains("ADMIN")) {
            showError("You don't have permission to access this page");
            // Redirect to user dashboard
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-list.fxml"));
                Parent root = loader.load();
                
                PostListController controller = loader.getController();
                controller.setCurrentUser(currentUser);
                
                Stage stage = (Stage) contentContainer.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("OncoKidsCare - Posts");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        // Initialize admin view
        showPendingPosts();
    }
    
    @FXML
    public void showPendingPosts() {
        currentView = "pending";
        if (contentTitle != null) {
            contentTitle.setText("Pending Posts");
        }
        
        try {
            // Clear container
            if (contentContainer != null) {
                contentContainer.getChildren().clear();
                
                // Add loading indicator to the container
                HBox loadingBox = new HBox(10);
                loadingBox.setAlignment(Pos.CENTER);
                loadingBox.getChildren().addAll(progressIndicator, statusLabel);
                contentContainer.getChildren().add(loadingBox);
                
                // Show loading indicator
                showLoadingIndicator(true, "Loading pending posts...");
                
                // Use a background thread to load posts
                Thread loadThread = new Thread(() -> {
                    try {
                        // Load pending posts
                        List<Post> pendingPosts = PostDAO.findByStatus("pending");
                        
                        // Update UI on JavaFX thread
                        javafx.application.Platform.runLater(() -> {
                            try {
                                // Hide loading indicator
                                showLoadingIndicator(false, null);
                                
                                // Clear container again (to remove loading indicator)
                                contentContainer.getChildren().clear();
                                
                                if (pendingPosts.isEmpty()) {
                                    Label noPostsLabel = new Label("No pending posts to approve");
                                    noPostsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
                                    contentContainer.getChildren().add(noPostsLabel);
                                } else {
                                    Label countLabel = new Label("Found " + pendingPosts.size() + " pending posts");
                                    countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 0 0 10 0;");
                                    contentContainer.getChildren().add(countLabel);
                                    
                                    for (Post post : pendingPosts) {
                                        VBox postView = createSimplePostView(post, true);
                                        contentContainer.getChildren().add(postView);
                                    }
                                }
                            } catch (Exception ex) {
                                showError("Error displaying posts: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        });
                    } catch (SQLException e) {
                        javafx.application.Platform.runLater(() -> {
                            showLoadingIndicator(false, null);
                            showError("Error loading pending posts: " + e.getMessage());
                            e.printStackTrace();
                        });
                    }
                });
                
                loadThread.setDaemon(true);
                loadThread.start();
            }
        } catch (Exception e) {
            showLoadingIndicator(false, null);
            showError("Error initializing posts view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void showApprovedPosts() {
        currentView = "approved";
        if (contentTitle != null) {
            contentTitle.setText("Approved Posts");
        }
        
        try {
            // Clear container
            if (contentContainer != null) {
                contentContainer.getChildren().clear();
                
                // Add loading indicator to the container
                HBox loadingBox = new HBox(10);
                loadingBox.setAlignment(Pos.CENTER);
                loadingBox.getChildren().addAll(progressIndicator, statusLabel);
                contentContainer.getChildren().add(loadingBox);
                
                // Show loading indicator
                showLoadingIndicator(true, "Loading approved posts...");
                
                // Use a background thread to load posts
                Thread loadThread = new Thread(() -> {
                    try {
                        // Load approved posts
                        List<Post> approvedPosts = PostDAO.findByStatus("approved");
                        
                        // Update UI on JavaFX thread
                        javafx.application.Platform.runLater(() -> {
                            try {
                                // Hide loading indicator
                                showLoadingIndicator(false, null);
                                
                                // Clear container again (to remove loading indicator)
                                contentContainer.getChildren().clear();
                                
                                if (approvedPosts.isEmpty()) {
                                    Label noPostsLabel = new Label("No approved posts to display");
                                    noPostsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
                                    contentContainer.getChildren().add(noPostsLabel);
                                } else {
                                    Label countLabel = new Label("Found " + approvedPosts.size() + " approved posts");
                                    countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 0 0 10 0;");
                                    contentContainer.getChildren().add(countLabel);
                                    
                                    for (Post post : approvedPosts) {
                                        VBox postView = createSimplePostView(post, false);
                                        contentContainer.getChildren().add(postView);
                                    }
                                }
                            } catch (Exception ex) {
                                showError("Error displaying posts: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        });
                    } catch (SQLException e) {
                        javafx.application.Platform.runLater(() -> {
                            showLoadingIndicator(false, null);
                            showError("Error loading approved posts: " + e.getMessage());
                            e.printStackTrace();
                        });
                    }
                });
                
                loadThread.setDaemon(true);
                loadThread.start();
            }
        } catch (Exception e) {
            showLoadingIndicator(false, null);
            showError("Error initializing posts view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private VBox createSimplePostView(Post post, boolean isPending) {
        VBox postBox = new VBox(10);
        postBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: white; -fx-background-radius: 5;");
        
        // User info section
        HBox userInfo = new HBox(10);
        userInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label userLabel = new Label(post.isAnonymous() ? "Anonymous User" : post.getUser().getNom() + " " + post.getUser().getPrenom());
        userLabel.setStyle("-fx-font-weight: bold;");
        
        Label dateLabel = new Label(post.getPublishDate().toString().replace("T", " ").substring(0, 16));
        dateLabel.setStyle("-fx-text-fill: #777;");
        
        // Add status label for pending posts
        Label statusLabel = null;
        if (isPending) {
            statusLabel = new Label("PENDING");
            statusLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 3;");
        }
        
        userInfo.getChildren().addAll(userLabel, new Label(" • "), dateLabel);
        if (statusLabel != null) {
            userInfo.getChildren().addAll(new Label(" • "), statusLabel);
        }
        
        // Content section
        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px;");
        
        // Category label
        Label categoryLabel = new Label(post.getCategory());
        categoryLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 3 8; -fx-background-radius: 3;");
        
        postBox.getChildren().addAll(userInfo, contentLabel, categoryLabel);
        
        // Add action buttons for pending posts
        if (isPending) {
            HBox buttons = new HBox(10);
            buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            buttons.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
            
            Button approveBtn = new Button("Approve");
            approveBtn.getStyleClass().add("approve-button");
            approveBtn.setOnAction(e -> handleApprovePost(post));
            
            Button rejectBtn = new Button("Reject");
            rejectBtn.getStyleClass().add("reject-button");
            rejectBtn.setOnAction(e -> handleRejectPost(post));
            
            buttons.getChildren().addAll(approveBtn, rejectBtn);
            postBox.getChildren().add(buttons);
        }
        
        return postBox;
    }
    
    @FXML
    public void switchToUserView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) contentScrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Posts");
        } catch (IOException e) {
            showError("Error switching to user view: " + e.getMessage());
        }
    }
    
    // Add these methods to handle post approval and rejection
    private void handleApprovePost(Post post) {
        try {
            // Update post status in database
            post.setStatus("approved");
            PostDAO.updateStatus(post.getId(), "approved");
            
            // Show success message
            showSuccess("Post Approved", "The post has been approved successfully.");
            
            // Refresh the view
            if (currentView.equals("pending")) {
                showPendingPosts();
            } else {
                showApprovedPosts();
            }
        } catch (SQLException e) {
            showError("Error approving post: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleRejectPost(Post post) {
        try {
            // Delete the post from database
            PostDAO.delete(post.getId());
            
            // Show success message
            showSuccess("Post Rejected", "The post has been rejected and removed.");
            
            // Refresh the view
            showPendingPosts();
        } catch (SQLException e) {
            showError("Error rejecting post: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Add this method to show success messages
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Add this method to set the current user (if not already present)
    private User currentUser;
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // If we already initialized the UI, load the posts
        if (contentContainer != null) {
            showPendingPosts();
        }
    }
    
    // Remove this duplicate initialize method
    
    // Add this method to show loading indicator
    private void showLoadingIndicator(boolean show, String message) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(show);
        }
        if (statusLabel != null) {
            if (message != null) {
                statusLabel.setText(message);
            }
            statusLabel.setVisible(show);
        }
    }
    
    // Add this method to show error messages
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Add this method to refresh content
    @FXML
    public void refreshContent() {
        // Determine which content is currently showing and refresh it
        if (currentView.equals("pending")) {
            showPendingPosts();
        } else if (currentView.equals("approved")) {
            showApprovedPosts();
        } else if (currentView.equals("dashboard")) {
            showDashboard();
        }
        
        // Show success message
        showSuccess("Refreshed", "Content has been refreshed successfully.");
    }

    // Add this method to show dashboard
    @FXML
    public void showDashboard() {
        currentView = "dashboard";
        if (contentTitle != null) {
            contentTitle.setText("Admin Dashboard");
        }
        
        // Clear the content container
        if (contentContainer != null) {
            contentContainer.getChildren().clear();
            
            // Show loading indicator
            showLoadingIndicator(true, "Loading dashboard...");
            
            // Load dashboard in a separate thread
            Thread loadThread = new Thread(() -> {
                try {
                    // Load dashboard data
                    loadDashboard();
                    
                    // Hide loading indicator on JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        showLoadingIndicator(false, null);
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        showLoadingIndicator(false, null);
                        showError("Error loading dashboard: " + e.getMessage());
                    });
                }
            });
            
            loadThread.setDaemon(true);
            loadThread.start();
        }
    }

    // Add this method to load dashboard
    private void loadDashboard() {
        // This would be run in a background thread
        try {
            // Get statistics
            int totalPosts = PostDAO.countAll();
            int pendingPosts = PostDAO.countByStatus("pending");
            int approvedPosts = PostDAO.countByStatus("approved");
            int totalComments = CommentDAO.countAll();
            
            // Create dashboard UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                // Create dashboard UI
                VBox dashboardContainer = new VBox(20);
                dashboardContainer.setPadding(new Insets(20));
                
                // Stats cards
                HBox statsCards = new HBox(20);
                statsCards.setAlignment(Pos.CENTER);
                
                // Total posts card
                VBox totalPostsCard = createStatsCard("Total Posts", totalPosts, "📝", "#3498db");
                
                // Pending posts card
                VBox pendingPostsCard = createStatsCard("Pending Posts", pendingPosts, "⏳", "#f39c12");
                
                // Approved posts card
                VBox approvedPostsCard = createStatsCard("Approved Posts", approvedPosts, "✅", "#2ecc71");
                
                // Total comments card
                VBox totalCommentsCard = createStatsCard("Total Comments", totalComments, "💬", "#9b59b6");
                
                statsCards.getChildren().addAll(totalPostsCard, pendingPostsCard, approvedPostsCard, totalCommentsCard);
                
                // Add to dashboard container
                dashboardContainer.getChildren().add(statsCards);
                
                // Add quick actions section
                Label actionsTitle = new Label("Quick Actions");
                actionsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 20 0 10 0;");
                
                HBox quickActions = new HBox(15);
                quickActions.setAlignment(Pos.CENTER_LEFT);
                
                Button viewPendingBtn = new Button("View Pending Posts");
                viewPendingBtn.getStyleClass().add("action-button");
                viewPendingBtn.setOnAction(e -> showPendingPosts());
                
                Button viewReportedBtn = new Button("View Reported Comments");
                viewReportedBtn.getStyleClass().add("action-button");
                viewReportedBtn.setOnAction(e -> showReportedComments());
                
                quickActions.getChildren().addAll(viewPendingBtn, viewReportedBtn);
                
                dashboardContainer.getChildren().addAll(actionsTitle, quickActions);
                
                // Add to content container
                contentContainer.getChildren().add(dashboardContainer);
            });
        } catch (SQLException e) {
            javafx.application.Platform.runLater(() -> {
                showError("Error loading dashboard data: " + e.getMessage());
            });
        }
    }

    // Helper method to create stats card
    private VBox createStatsCard(String title, int value, String icon, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 10);");
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 36px;");
        
        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        
        return card;
    }
    
    // Add this method to show reported comments
    // Remove the backToUserMode method if it's no longer needed
    
    // Keep other methods like showReportedComments
    @FXML
    public void showReportedComments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/reported-comments.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) contentScrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Reported Comments Management");
        } catch (IOException e) {
            showError("Error opening reported comments view: " + e.getMessage());
        }
    }
    
    // Add this method to handle the backToUserMode action
    @FXML
    public void backToUserMode() {
        try {
            // Get the current user from session
            User user = Session.getUtilisateurConnecte();
            
            // Load the user dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-list.fxml"));
            Parent root = loader.load();
            
            // Pass the user to the controller
            PostListController controller = loader.getController();
            if (controller != null && user != null) {
                controller.setCurrentUser(user);
            }
            
            // Switch to the user view
            Stage stage = (Stage) contentScrollPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OncoKidsCare - Posts");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Error switching to user mode: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Add this method to navigate to dashboard
    @FXML
    public void navigateToDashboard() {
        try {
            // Get the current user from session
            User user = Session.getUtilisateurConnecte();
            
            // Load the dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/dashboard.fxml"));
            Parent root = loader.load();
            
            // Pass the user to the controller
            DashboardController controller = loader.getController();
            if (controller != null && user != null) {
                controller.initData(user);
            }
            
            // Switch to the dashboard view
            Stage stage = (Stage) contentScrollPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Error navigating to dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}