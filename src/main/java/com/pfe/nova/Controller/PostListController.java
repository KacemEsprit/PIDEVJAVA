package com.pfe.nova.Controller;

import com.pfe.nova.configuration.CommentReportDAO;
import com.pfe.nova.models.Post;
import com.pfe.nova.models.Comment;
import com.pfe.nova.configuration.PostDAO;
import com.pfe.nova.configuration.CommentDAO;
import com.pfe.nova.configuration.LikeDAO;
import com.pfe.nova.models.Like;
import com.pfe.nova.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class PostListController {
    // Add missing FXML field declarations
    @FXML private VBox postsContainer;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ToggleButton adminModeToggle;
    @FXML private VBox adminSidebar;
    @FXML private VBox pendingPostsContainer;
    @FXML private CheckBox showPendingPosts;

    // Add missing field declarations
    private boolean isAdminMode = false;
    private int currentUserId;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.currentUserId = user.getId();
        loadPosts(); // Load posts immediately when user is set
    }

    @FXML
    public void initialize() {
        setupCategoryFilter();
        
        // Add listener for the pending posts checkbox
        if (showPendingPosts != null) {
            showPendingPosts.selectedProperty().addListener((obs, oldVal, newVal) -> {
                loadPosts();
            });
        }
        // We'll load posts when the user is set
    }

    // Remove the toggleAdminMode method
    // @FXML private void toggleAdminMode() { ... }

    @FXML
    private void switchToAdminPostsManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/admin-posts-management.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Posts Management");
        } catch (IOException e) {
            showError("Error opening admin posts management: " + e.getMessage());
        }
    }

    // Add the missing setupCategoryFilter method
    private void setupCategoryFilter() {
        categoryFilter.getItems().addAll(
                "All",
                "Témoignage",
                "Question médicale",
                "Conseil",
                "Autre"
        );
        categoryFilter.setValue("All");
        categoryFilter.setOnAction(e -> loadPosts());
    }

    // Modify the toggleAdminMode method
    @FXML
    private void toggleAdminMode() {
        isAdminMode = adminModeToggle.isSelected();

        if (isAdminMode) {
            // Switch to admin posts management
            switchToAdminPostsManagement();
        } else {
            // Just reload posts in user mode - use current user instead of hardcoded ID
            loadPosts();
        }
    }

    @FXML
    private void setupAdminView() {
        // Clear the regular posts container
        postsContainer.getChildren().clear();

        // Show admin sidebar if it exists
        if (adminSidebar != null) {
            adminSidebar.setVisible(true);
        }

        // Load pending posts for approval
        loadPendingPosts();
    }

    @FXML
    private void loadPendingPosts() {
        try {
            if (pendingPostsContainer != null) {
                pendingPostsContainer.getChildren().clear();

                // Get all pending posts
                List<Post> pendingPosts = PostDAO.findByStatus("pending");

                if (pendingPosts.isEmpty()) {
                    Label noPostsLabel = new Label("No pending posts to approve");
                    noPostsLabel.getStyleClass().add("no-posts-label");
                    pendingPostsContainer.getChildren().add(noPostsLabel);
                } else {
                    for (Post post : pendingPosts) {
                        pendingPostsContainer.getChildren().add(createAdminPostView(post));
                    }
                }
            }
        } catch (SQLException e) {
            showError("Error loading pending posts: " + e.getMessage());
        }
    }

    @FXML
    // Change from private to public
    public VBox createAdminPostView(Post post) {
        VBox postBox = new VBox(10);
        postBox.getStyleClass().add("admin-post-card");
        postBox.setPadding(new Insets(15));

        // Post header with user info
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label userLabel = new Label("User: " + post.getUser().getNom() + " " + post.getUser().getPrenom());
        userLabel.getStyleClass().add("admin-post-user");

        Label categoryLabel = new Label("Category: " + post.getCategory());
        categoryLabel.getStyleClass().add("admin-post-category");

        header.getChildren().addAll(userLabel, categoryLabel);

        // Post content
        Label contentLabel = new Label(post.getContent());
        contentLabel.getStyleClass().add("admin-post-content");
        contentLabel.setWrapText(true);

        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        Button approveButton = new Button("Approve");
        approveButton.getStyleClass().add("approve-button");
        approveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

        Button rejectButton = new Button("Reject");
        rejectButton.getStyleClass().add("reject-button");
        rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        approveButton.setOnAction(e -> handleApprovePost(post));
        rejectButton.setOnAction(e -> handleRejectPost(post));

        actionButtons.getChildren().addAll(approveButton, rejectButton);

        // Add all components to post box
        postBox.getChildren().addAll(header, contentLabel);

        // Add images if any
        if (!post.getImageUrls().isEmpty()) {
            FlowPane imagePane = new FlowPane(10, 10);
            imagePane.getStyleClass().add("admin-post-images");

            for (String imagePath : post.getImageUrls()) {
                try {
                    Image image;
                    if (imagePath.startsWith("http")) {
                        image = new Image(imagePath);
                    } else {
                        image = new Image(new File(imagePath).toURI().toString());
                    }

                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);
                    imageView.setPreserveRatio(true);

                    imagePane.getChildren().add(imageView);
                } catch (Exception e) {
                    System.err.println("Error loading image: " + e.getMessage());
                }
            }

            postBox.getChildren().add(imagePane);
        }

        postBox.getChildren().add(actionButtons);

        return postBox;
    }

    @FXML
    private void handleApprovePost(Post post) {
        // Create a simple confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Approve Post");
        confirmation.setHeaderText("Approve Post");
        confirmation.setContentText("Are you sure you want to approve this post?");

        // Use the standard OK and Cancel buttons
        Optional<ButtonType> result = confirmation.showAndWait();

        // Check if OK was pressed
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Update the post status in the database
                post.setStatus("approved");
                PostDAO.updateStatus(post.getId(), "approved");

                // Show success message
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Post has been approved successfully!");
                success.showAndWait();

                // Refresh the pending posts list
                loadPendingPosts();
            } catch (SQLException e) {
                showError("Error approving post: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for debugging
            }
        }
    }

    private void handleRejectPost(Post post) {
        // Create a simple confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Reject Post");
        confirmation.setHeaderText("Reject Post");
        confirmation.setContentText("Are you sure you want to reject this post? This will delete the post permanently.");

        // Use the standard OK and Cancel buttons
        Optional<ButtonType> result = confirmation.showAndWait();

        // Check if OK was pressed
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                PostDAO.delete(post.getId());

                // Show success message
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Post has been rejected and deleted successfully!");
                success.showAndWait();

                loadPendingPosts(); // Refresh the pending posts list
            } catch (SQLException e) {
                showError("Error rejecting post: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for debugging
            }
        }
    }

    // Modify the loadPosts method to handle the checkbox
    // Update the loadPosts method to use currentUser consistently
    private void loadPosts() {
        try {
            postsContainer.getChildren().clear();
            String selectedCategory = categoryFilter.getValue();

            // Hide admin sidebar if it exists
            if (adminSidebar != null) {
                adminSidebar.setVisible(false);
            }

            List<Post> posts;
            
            // Check if we should show pending posts for the current user
            if (showPendingPosts != null && showPendingPosts.isSelected() && currentUser != null) {
                // Use the method that shows both approved posts and user's pending posts
                posts = PostDAO.findApprovedAndUserPending(currentUser.getId());
            } else if (currentUser != null && currentUser.getId() == 3) { // Check if current user is admin
                // Admin sees all posts
                posts = PostDAO.findAll();
            } else {
                // Regular users see only approved posts
                posts = PostDAO.findByStatus("approved");
            }

            // Sort posts by date (newest first)
            posts.sort((post1, post2) -> post2.getPublishDate().compareTo(post1.getPublishDate()));

            boolean isFirst = true;
            for (Post post : posts) {
                // Filter by category if needed
                if (selectedCategory == null || selectedCategory.equals("All") || selectedCategory.equals(post.getCategory())) {
                    // For pending posts belonging to current user, show a special indicator
                    if (post.getStatus().equals("pending") && currentUser != null && post.getUser().getId() == currentUser.getId()) {
                        VBox pendingNotice = createPendingPostNotice(post);
                        postsContainer.getChildren().add(pendingNotice);
                    } else if (post.getStatus().equals("approved")) {
                        // Only show approved posts to everyone
                        if (!isFirst) {
                            Separator separator = new Separator();
                            separator.getStyleClass().add("post-separator");
                            postsContainer.getChildren().add(separator);
                        }
                        postsContainer.getChildren().add(createPostView(post));
                        isFirst = false;
                    }
                }
            }
        } catch (SQLException e) {
            showError("Error loading posts: " + e.getMessage());
        }
    }

    // Add this method to create a notice for pending posts
    private VBox createPendingPostNotice(Post post) {
        VBox noticeBox = new VBox(10);
        noticeBox.getStyleClass().add("pending-notice");
        noticeBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ffc107; -fx-border-width: 2; -fx-padding: 15; -fx-border-radius: 5;");

        Label statusLabel = new Label("Your post is pending approval");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffc107;");

        Label contentPreview = new Label(post.getContent().length() > 50 ?
                post.getContent().substring(0, 50) + "..." : post.getContent());
        contentPreview.setWrapText(true);

        noticeBox.getChildren().addAll(statusLabel, contentPreview);

        return noticeBox;
    }

    private VBox createPostView(Post post) {
        VBox postBox = new VBox(15);
        postBox.getStyleClass().addAll("post-box", "post-card");  // Add both classes
        postBox.setPadding(new Insets(20));

        // Header with category and author
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label categoryLabel = new Label(post.getCategory());
        categoryLabel.getStyleClass().add("post-category");

        Label authorLabel = new Label("Posted by " +
                (post.isAnonymous() ? "Anonymous" : post.getUser().getNom()));
        authorLabel.getStyleClass().add("post-author");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        // Format the date properly
        String formattedDate = post.getPublishDate().toLocalDate().format(
                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        Label dateLabel = new Label(formattedDate);
        dateLabel.getStyleClass().add("post-date");

        header.getChildren().addAll(categoryLabel, authorLabel, headerSpacer, dateLabel);

        // Content
        Label contentLabel = new Label(post.getContent());
        contentLabel.getStyleClass().add("post-content");
        contentLabel.setWrapText(true);

        // Images with improved layout
        FlowPane imagePane = new FlowPane(15, 15);
        imagePane.getStyleClass().add("post-images");

        for (String imagePath : post.getImageUrls()) {
            try {
                Image image;
                if (imagePath.startsWith("http")) {
                    image = new Image(imagePath);
                } else {
                    image = new Image(new File(imagePath).toURI().toString());
                }

                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(150);
                imageView.setFitWidth(150);
                imageView.setPreserveRatio(true);
                imageView.getStyleClass().add("post-image");

                // Add a container for the image with styling
                VBox imageContainer = new VBox(imageView);
                imageContainer.getStyleClass().add("image-container");
                imageContainer.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 5; -fx-background-radius: 5;");

                imagePane.getChildren().add(imageContainer);
            } catch (Exception e) {
                System.err.println("Error loading image: " + imagePath + " - " + e.getMessage());
            }
        }

        // Actions section with modern styling
        HBox actionsBox = new HBox(20);
        actionsBox.getStyleClass().add("post-actions");
        actionsBox.setAlignment(Pos.CENTER_LEFT);

        // Reaction section (likes)
        HBox reactionBox = new HBox(15);
        reactionBox.getStyleClass().add("post-actions");
        reactionBox.setAlignment(Pos.CENTER_LEFT);
        reactionBox.setPadding(new Insets(10, 0, 10, 0));

        // Get like count and user like status
        int likeCount = 0;
        boolean userHasLiked = false;
        try {
            likeCount = LikeDAO.countLikes(post.getId());
            // Use current user ID instead of hardcoded value
            userHasLiked = currentUser != null ? LikeDAO.hasUserLiked(post.getId(), currentUser.getId()) : false;
        } catch (SQLException e) {
            System.err.println("Error loading likes: " + e.getMessage());
        }

        // Create styled like button
        ToggleButton likeButton = new ToggleButton();
        likeButton.getStyleClass().add("post-action-button");

        if (userHasLiked) {
            likeButton.setSelected(true);
            likeButton.setText("❤ Liked");
            likeButton.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            likeButton.setText("♡ Like");
        }

        // Like count label
        Label likeCountLabel = new Label(likeCount + " likes");
        likeCountLabel.getStyleClass().add("post-like-count");

        likeButton.setOnAction(e -> {
            try {
                Like like = new Like();
                like.setPublicationId(post.getId());
                // Use current user ID instead of hardcoded value
                like.setUserId(currentUser != null ? currentUser.getId() : 0);

                LikeDAO.save(like);

                // Update UI
                if (likeButton.isSelected()) {
                    likeButton.setText("❤ Liked");
                    likeButton.setStyle("-fx-text-fill: #e74c3c;");
                    int currentLikes = Integer.parseInt(likeCountLabel.getText().split(" ")[0]);
                    likeCountLabel.setText((currentLikes + 1) + " likes");
                } else {
                    likeButton.setText("♡ Like");
                    likeButton.setStyle("");
                    int currentLikes = Integer.parseInt(likeCountLabel.getText().split(" ")[0]);
                    if (currentLikes > 0) {
                        likeCountLabel.setText((currentLikes - 1) + " likes");
                    }
                }
            } catch (SQLException ex) {
                System.err.println("Error updating like: " + ex.getMessage());
            }
        });

        // Comment button
        Button commentBtn = new Button("💬 Comment");
        commentBtn.getStyleClass().add("post-action-button");


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action buttons
        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("post-action-button");

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("post-action-button");
        deleteButton.setStyle("-fx-text-fill: #e74c3c;");

        // Only show edit/delete buttons if the post belongs to current user
        if (currentUser != null && post.getUser().getId() == currentUser.getId()) {
            editButton.setOnAction(e -> handleEditPost(post));
            deleteButton.setOnAction(e -> handleDeletePost(post));
            reactionBox.getChildren().addAll(likeButton, likeCountLabel, commentBtn, spacer, editButton, deleteButton);
        } else {
            // For other users' posts, only show like and comment buttons
            reactionBox.getChildren().addAll(likeButton, likeCountLabel, commentBtn);
        }

        // Comments section
        VBox commentsBox = new VBox(10);
        commentsBox.getStyleClass().add("comments-container");
        commentsBox.setPadding(new Insets(10, 0, 0, 0));

        // Comment input
        HBox commentInput = new HBox(10);
        commentInput.setAlignment(Pos.CENTER_LEFT);
        commentInput.getStyleClass().add("comment-input");

        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment...");
        commentField.setPrefWidth(300);
        commentField.getStyleClass().add("comment-field");
        HBox.setHgrow(commentField, Priority.ALWAYS);

        Button submitComment = new Button("Post");
        submitComment.getStyleClass().add("comment-submit-button");
        submitComment.setOnAction(e -> handleAddComment(post.getId(), commentField));

        commentInput.getChildren().addAll(commentField, submitComment);

        // Add a label for comments section
        Label commentsLabel = new Label("Comments");
        commentsLabel.getStyleClass().add("comments-header");
        commentsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5 0;");

        commentsBox.getChildren().addAll(commentsLabel, commentInput);

        // Add separator
        Separator separator = new Separator();
        separator.getStyleClass().add("comment-separator");
        commentsBox.getChildren().add(separator);

        // Load existing comments
        try {
            List<Comment> comments = CommentDAO.findByPostId(post.getId());
            for (Comment comment : comments) {
                HBox commentBox = createCommentView(comment);
                commentsBox.getChildren().add(commentBox);
            }
        } catch (SQLException ex) {
            System.err.println("Error loading comments: " + ex.getMessage());
        }

        // Make the post clickable
        postBox.setOnMouseClicked(e -> {
            if (e.getTarget() != commentField && e.getTarget() != submitComment
                    && e.getTarget() != editButton && e.getTarget() != deleteButton
                    && e.getTarget() != likeButton) {
                openPublicationDetails(post);
            }
        });

        // Add all components to the post box in correct order
        postBox.getChildren().addAll(header, contentLabel);

        if (!post.getImageUrls().isEmpty()) {
            postBox.getChildren().add(imagePane);
        }

        postBox.getChildren().addAll(reactionBox, commentsBox);

        return postBox;
    }

    private HBox createCommentView(Comment comment) {
        HBox commentBox = new HBox(10);
        commentBox.getStyleClass().add("comment-box");
        commentBox.setPadding(new Insets(10));

        // Avatar placeholder (you can replace with actual user avatar)
        Region avatarPlaceholder = new Region();
        avatarPlaceholder.setPrefSize(32, 32);
        avatarPlaceholder.setMinSize(32, 32);
        avatarPlaceholder.setMaxSize(32, 32);
        avatarPlaceholder.getStyleClass().add("comment-avatar");
        avatarPlaceholder.setStyle("-fx-background-color: #3498db; -fx-background-radius: 16;");

        VBox commentContent = new VBox(5);
        commentContent.setPrefWidth(Region.USE_COMPUTED_SIZE);
        commentContent.getStyleClass().add("comment-content-box");
        commentContent.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 10; -fx-padding: 8;");

        Label authorLabel = new Label(comment.getUser().getNom());
        authorLabel.getStyleClass().add("comment-author");
        authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Label contentLabel = new Label(comment.getContenuCom());
        contentLabel.getStyleClass().add("comment-text");
        contentLabel.setWrapText(true);

        // Add reported badge if comment is reported
        if (comment.isReported()) {
            Label reportedBadge = new Label("⚠ Reported");
            reportedBadge.getStyleClass().add("reported-badge");
            reportedBadge.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-padding: 2 5; -fx-background-radius: 3;");
            commentContent.getChildren().addAll(authorLabel, contentLabel, reportedBadge);
        } else {
            commentContent.getChildren().addAll(authorLabel, contentLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox buttonsBox = new VBox(5);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.getStyleClass().add("comment-buttons");

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("comment-action-button");
        editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #1a73e8;");
        editButton.setOnAction(e -> handleEditComment(comment));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("comment-action-button");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c;");
        deleteButton.setOnAction(e -> handleDeleteComment(comment));

        // Add report button
        Button reportButton = new Button("⚠ Report");
        reportButton.getStyleClass().add("comment-action-button");
        reportButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff9800;");

        // Check if current user has already reported this comment
        try {
            // Use currentUser.getId() instead of currentUserId
            boolean hasReported = CommentReportDAO.hasUserReported(comment.getId(), 
                currentUser != null ? currentUser.getId() : 0);
            if (hasReported) {
                reportButton.setText("✓ Reported");
                reportButton.setDisable(true);
            }
        } catch (SQLException ex) {
            System.err.println("Error checking report status: " + ex.getMessage());
        }

        reportButton.setOnAction(e -> handleReportComment(comment));

        // Only show edit/delete buttons if the comment belongs to current user
        // Use currentUser.getId() instead of currentUserId
        if (currentUser != null && comment.getUserId() == currentUser.getId()) {
            buttonsBox.getChildren().addAll(editButton, deleteButton);
        } else {
            // Only show report button for comments from other users
            buttonsBox.getChildren().add(reportButton);
        }

        commentBox.getChildren().addAll(avatarPlaceholder, commentContent, spacer, buttonsBox);

        return commentBox;
    }

    // Add method to handle comment reporting
    private void handleReportComment(Comment comment) {
        // Create a dialog to select report reason
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Report Comment");
        dialog.setHeaderText("Why are you reporting this comment?");

        // Set the button types
        ButtonType reportButtonType = new ButtonType("Report", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reportButtonType, ButtonType.CANCEL);

        // Create the radio buttons for report reasons
        ToggleGroup group = new ToggleGroup();

        RadioButton rb1 = new RadioButton("Inappropriate content");
        rb1.setToggleGroup(group);
        rb1.setSelected(true);
        rb1.setUserData("contenu_inapproprie");

        RadioButton rb2 = new RadioButton("Harassment");
        rb2.setToggleGroup(group);
        rb2.setUserData("harcelement");

        RadioButton rb3 = new RadioButton("Spam");
        rb3.setToggleGroup(group);
        rb3.setUserData("spam");

        RadioButton rb4 = new RadioButton("False information");
        rb4.setToggleGroup(group);
        rb4.setUserData("fausse_information");

        // Create layout for dialog
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(rb1, rb2, rb3, rb4);
        vbox.setPadding(new Insets(20, 10, 10, 10));

        dialog.getDialogPane().setContent(vbox);

        // Convert the result to a string when the report button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reportButtonType) {
                return group.getSelectedToggle().getUserData().toString();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(reason -> {
            try {
                // Add debugging output
                System.out.println("Reporting comment ID: " + comment.getId() + 
                    ", User ID: " + (currentUser != null ? currentUser.getId() : 0) + 
                    ", Reason: " + reason);

                // Create a new comment report - use currentUser.getId()
                CommentReportDAO.reportComment(comment.getId(), 
                    currentUser != null ? currentUser.getId() : 0, reason);

                // Update the comment's reported status
                comment.setReported(true);
                comment.setReportReason(reason);
                CommentDAO.updateReportStatus(comment.getId(), true, reason);

                // Show confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Comment Reported");
                alert.setHeaderText(null);
                alert.setContentText("Thank you for your report. Our moderators will review it.");
                alert.showAndWait();

                // Refresh the view
                loadPosts();
            } catch (SQLException ex) {
                // Improve error handling with more details
                System.err.println("Error reporting comment: " + ex.getMessage());
                ex.printStackTrace();
                showError("Error reporting comment: " + ex.getMessage());
            }
        });
    }

    private void handleEditComment(Comment comment) {
        TextInputDialog dialog = new TextInputDialog(comment.getContenuCom());
        dialog.setTitle("Edit Comment");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter your comment:");

        dialog.showAndWait().ifPresent(newContent -> {
            try {
                comment.setContenuCom(newContent);
                CommentDAO.save(comment);
                loadPosts(); // Refresh to show edited comment
            } catch (SQLException ex) {
                showError("Error updating comment: " + ex.getMessage());
            }
        });
    }

    // Update the handleAddComment method to use currentUser
    private void handleAddComment(int postId, TextField commentField) {
        String content = commentField.getText().trim();
        if (content.isEmpty()) return;

        try {
            Comment comment = new Comment();
            comment.setPublicationId(postId);
            // Use current user ID instead of hardcoded value
            comment.setUserId(currentUser != null ? currentUser.getId() : 0);
            comment.setContenuCom(content);

            CommentDAO.save(comment);
            commentField.clear();
            loadPosts(); // Refresh to show new comment
        } catch (SQLException ex) {
            showError("Error adding comment: " + ex.getMessage());
        }
    }

    private void handleDeleteComment(Comment comment) {
        try {
            CommentDAO.delete(comment.getId());
            loadPosts(); // Refresh to remove deleted comment
        } catch (SQLException ex) {
            showError("Error deleting comment: " + ex.getMessage());
        }
    }

    // Add this method to handle creating a new post
    @FXML
    // Update the handleNewPost method to pass the current user
    private void handleNewPost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-form.fxml"));
            Parent root = loader.load();

            // Get the controller and set the current user
            PostFormController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Create New Post");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh the posts list after creating a new post
            loadPosts();
        } catch (IOException e) {
            showError("Error opening post form: " + e.getMessage());
        }
    }

    // Update the handleEditPost method to pass the current user
    private void handleEditPost(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-form.fxml"));
            Parent root = loader.load();

            PostFormController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setEditMode(post);

            Stage stage = new Stage();
            stage.setTitle("Edit Post");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadPosts(); // Refresh the list after editing
        } catch (IOException e) {
            showError("Error opening edit form: " + e.getMessage());
        }
    }

    private void handleDeletePost(Post post) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Post");
        confirmation.setHeaderText("Are you sure you want to delete this post?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    PostDAO.delete(post.getId());
                    loadPosts(); // Refresh the list after deletion
                } catch (SQLException ex) {
                    showError("Error deleting post: " + ex.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Add this method to allow switching to user mode from admin view
    // Remove this hardcoded user ID in setUserMode method
    public void setUserMode() {
        // Remove hardcoded ID (2) and use the current user instead
        isAdminMode = false;
        if (adminModeToggle != null) {
            adminModeToggle.setSelected(false);
        }
        loadPosts();
    }

    private void openPublicationDetails(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/publication-details.fxml"));
            Parent root = loader.load();

            PublicationDetailsController controller = loader.getController();
            controller.setPost(post);

            Stage stage = new Stage();
            stage.setTitle("Publication Details");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error opening publication details: " + e.getMessage());
        }
    }


}