package com.pfe.nova.Controller;

import com.pfe.nova.models.Post;
import com.pfe.nova.models.Comment;
import com.pfe.nova.configuration.PostDAO;
import com.pfe.nova.configuration.CommentDAO;
import com.pfe.nova.configuration.LikeDAO;
import com.pfe.nova.models.Like;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
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
public class PostListController {
    @FXML private ComboBox<String> categoryFilter;
    @FXML private VBox postsContainer;

    @FXML
    public void initialize() {
        setupCategoryFilter();
        loadPosts();
    }

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

    @FXML
    private void handleNewPost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Post");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadPosts(); // Refresh the list after creating a new post
        } catch (IOException e) {
            showError("Error opening post form: " + e.getMessage());
        }
    }

    private void loadPosts() {
        try {
            postsContainer.getChildren().clear();
            String selectedCategory = categoryFilter.getValue();

            boolean isFirst = true;
            for (Post post : PostDAO.findAll()) {
                if (selectedCategory.equals("All") || selectedCategory.equals(post.getCategory())) {
                    if (!isFirst) {
                        Separator separator = new Separator();
                        separator.getStyleClass().add("post-separator");
                        postsContainer.getChildren().add(separator);
                    }
                    postsContainer.getChildren().add(createPostView(post));
                    isFirst = false;
                }
            }
        } catch (SQLException e) {
            showError("Error loading posts: " + e.getMessage());
        }
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
                (post.isAnonymous() ? "Anonymous" : post.getUser().getUsername()));
        authorLabel.getStyleClass().add("post-author");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Label dateLabel = new Label("Today"); // Replace with actual date
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
            userHasLiked = LikeDAO.hasUserLiked(post.getId(), 4); // Replace with actual logged-in user ID
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
                like.setUserId(4); // Replace with actual logged-in user ID
                
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
        
        Button deleteButton = new Button("Delete");  // Add this line
        deleteButton.getStyleClass().add("post-action-button");  // Add this line
        deleteButton.setStyle("-fx-text-fill: #e74c3c;");
        
        editButton.setOnAction(e -> handleEditPost(post));
        deleteButton.setOnAction(e -> handleDeletePost(post));
        
        reactionBox.getChildren().addAll(likeButton, likeCountLabel, commentBtn, spacer, editButton, deleteButton);
        
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
        
        Label authorLabel = new Label(comment.getUser().getUsername());
        authorLabel.getStyleClass().add("comment-author");
        authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        Label contentLabel = new Label(comment.getContenuCom());
        contentLabel.getStyleClass().add("comment-text");
        contentLabel.setWrapText(true);
        
        commentContent.getChildren().addAll(authorLabel, contentLabel);
        
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
        
        buttonsBox.getChildren().addAll(editButton, deleteButton);
        
        commentBox.getChildren().addAll(avatarPlaceholder, commentContent, spacer, buttonsBox);
        
        return commentBox;
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

    private void handleAddComment(int postId, TextField commentField) {
        String content = commentField.getText().trim();
        if (content.isEmpty()) return;

        try {
            Comment comment = new Comment();
            comment.setPublicationId(postId);
            comment.setUserId(4); // Currently logged in user
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

    private void handleEditPost(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-form.fxml"));
            Parent root = loader.load();

            PostFormController controller = loader.getController();
            controller.setEditMode(post); // You'll need to add this method to PostFormController

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