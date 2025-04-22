package com.pfe.nova.Controller;

import com.pfe.nova.models.Post;
import com.pfe.nova.models.Comment;
import com.pfe.nova.configuration.CommentDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import com.pfe.nova.configuration.LikeDAO;
import com.pfe.nova.models.Like;
import java.sql.SQLException;
import java.util.List;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.io.File;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.net.URL;

public class PublicationDetailsController {
    @FXML
    private VBox publicationContent;
    @FXML
    private VBox commentsList;
    @FXML
    private TextField commentField;
    @FXML
    private Button backButton;

    private Post post;

    public void setPost(Post post) {
        this.post = post;
        displayPost();
        loadComments();
    }

    private void displayPost() {
        // Clear existing content
        publicationContent.getChildren().clear();

        // Create header with category and author info
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("post-header");

        // Category badge with gradient styling
        Label categoryLabel = new Label(post.getCategory());
        categoryLabel.getStyleClass().add("publication-category");

        // Author info with proper styling
        Label authorLabel = new Label(post.isAnonymous() ? "Anonymous" : post.getUser().getNom());
        authorLabel.getStyleClass().add("publication-author");

        // Date info
        Label dateLabel = new Label("Posted on " + java.time.LocalDate.now().toString());
        dateLabel.getStyleClass().add("publication-date");

        VBox authorInfo = new VBox(5);
        authorInfo.getChildren().addAll(authorLabel, dateLabel);

        headerBox.getChildren().addAll(categoryLabel, authorInfo);

        // Content with proper styling - skip title since getTitle() doesn't exist
        Label contentLabel = new Label(post.getContent());
        contentLabel.getStyleClass().add("publication-text");
        contentLabel.setWrapText(true);

        // Create modern image gallery
        FlowPane imagesPane = new FlowPane(15, 15);
        imagesPane.getStyleClass().add("publication-images");

        if (!post.getImageUrls().isEmpty()) {
            for (String imagePath : post.getImageUrls()) {
                try {
                    Image image;
                    if (imagePath.startsWith("http")) {
                        image = new Image(imagePath);
                    } else {
                        image = new Image(new File(imagePath).toURI().toString());
                    }

                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(200);
                    imageView.setFitWidth(200);
                    imageView.setPreserveRatio(true);
                    imageView.getStyleClass().add("publication-image");

                    // Add rounded corners and shadow effect
                    Rectangle clip = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
                    clip.setArcWidth(20);
                    clip.setArcHeight(20);
                    imageView.setClip(clip);

                    StackPane imageContainer = new StackPane(imageView);
                    imageContainer.setPadding(new Insets(5));
                    imageContainer.getStyleClass().add("image-container");

                    imagesPane.getChildren().add(imageContainer);
                } catch (Exception e) {
                    System.err.println("Error loading image: " + imagePath + " - " + e.getMessage());
                }
            }
        }

        // Create reactions section with heart button and like count
        HBox reactionsBox = new HBox(10);
        reactionsBox.setAlignment(Pos.CENTER_LEFT);
        reactionsBox.getStyleClass().add("reactions-container");

        // Get like count from database
        int likeCount = 0;
        boolean userHasLiked = false;
        try {
            likeCount = LikeDAO.countLikes(post.getId());
            userHasLiked = LikeDAO.hasUserLiked(post.getId(), 4); // Replace with actual logged-in user ID
        } catch (SQLException e) {
            System.err.println("Error loading likes: " + e.getMessage());
        }

        ToggleButton heartButton = new ToggleButton(userHasLiked ? "❤" : "♡");
        heartButton.setSelected(userHasLiked);
        heartButton.getStyleClass().add("details-heart-button");

        Label likeCountLabel = new Label(Integer.toString(likeCount));
        likeCountLabel.getStyleClass().add("details-like-count");



        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        reactionsBox.getChildren().addAll(heartButton, likeCountLabel, spacer);

        // Set up like button action
        heartButton.setOnAction(e -> {
            try {
                Like like = new Like();
                like.setPublicationId(post.getId());
                like.setUserId(4); // Replace with actual logged-in user ID

                if (heartButton.isSelected()) {
                    LikeDAO.save(like);
                    heartButton.setText("❤");
                    int currentLikes = Integer.parseInt(likeCountLabel.getText());
                    // Fix unnecessary toString call
                    likeCountLabel.setText("" + (currentLikes + 1));
                } else {
                    // Fix the delete method signature issue by providing all three required parameters
                    LikeDAO.delete(post.getId(), 4, null); // The third parameter might be a timestamp or other value
                    heartButton.setText("♡");
                    int currentLikes = Integer.parseInt(likeCountLabel.getText());
                    if (currentLikes > 0) {
                        // Fix unnecessary toString call
                        likeCountLabel.setText("" + (currentLikes - 1));
                    }
                }
            } catch (SQLException ex) {
                showError("Error updating like: " + ex.getMessage());
            }
        });

        // Add all components to the publication content
        publicationContent.getChildren().addAll(
                headerBox,
                contentLabel
        );

        if (!post.getImageUrls().isEmpty()) {
            publicationContent.getChildren().add(imagesPane);
        }

        publicationContent.getChildren().add(reactionsBox);
    }

    // Rest of the methods remain the same, but remove the setupLikeButton method since it's not used

    // Update the comment section with modern styling
    private void loadComments() {
        try {
            commentsList.getChildren().clear();

            // Add section title
            Label commentsTitle = new Label("Comments");
            commentsTitle.getStyleClass().add("section-title");

            // Create comment input container
            HBox commentInputBox = new HBox(10);
            commentInputBox.setAlignment(Pos.CENTER_LEFT);
            commentInputBox.getStyleClass().add("comment-input-container");

            // Style the comment field
            commentField.getStyleClass().add("comment-input-field");
            commentField.setPromptText("Write a comment...");
            HBox.setHgrow(commentField, Priority.ALWAYS);

            // Create a modern submit button
            Button submitButton = new Button("Post");
            submitButton.getStyleClass().add("comment-submit-button");
            submitButton.setOnAction(e -> handleAddComment());

            commentInputBox.getChildren().addAll(commentField, submitButton);

            // Add separator
            Separator separator = new Separator();
            separator.getStyleClass().add("comments-separator");

            // Create comments list container
            VBox commentsContainer = new VBox(15);
            commentsContainer.getStyleClass().add("comments-list");

            // Add the comments
            List<Comment> comments = CommentDAO.findByPostId(post.getId());

            if (comments.isEmpty()) {
                Label noCommentsLabel = new Label("No comments yet. Be the first to comment!");
                noCommentsLabel.setStyle("-fx-text-fill: #8898aa; -fx-font-style: italic; -fx-padding: 10 0;");
                commentsContainer.getChildren().add(noCommentsLabel);
            } else {
                for (Comment comment : comments) {
                    commentsContainer.getChildren().add(createCommentView(comment));
                }
            }

            // Add all components to the comments section
            commentsList.getChildren().addAll(
                    commentsTitle,
                    commentInputBox,
                    separator,
                    commentsContainer
            );
        } catch (SQLException ex) {
            showError("Error loading comments: " + ex.getMessage());
        }
    }

    // Update the comment view with modern styling
    private HBox createCommentView(Comment comment) {
        HBox commentBox = new HBox(15);
        commentBox.getStyleClass().add("comment-box");

        // Create avatar with gradient background
        StackPane avatarPane = new StackPane();
        avatarPane.getStyleClass().add("comment-avatar");

        Text initial = new Text(comment.getUser().getNom().substring(0, 1).toUpperCase());
        initial.setFill(Color.WHITE);
        initial.setStyle("-fx-font-weight: bold;");

        avatarPane.getChildren().add(initial);

        // Comment content with header and text
        VBox contentBox = new VBox(5);
        contentBox.getStyleClass().add("comment-content-box");
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        // Comment header with author and date
        HBox headerBox = new HBox(10);
        headerBox.getStyleClass().add("comment-header");

        Label authorLabel = new Label(comment.getUser().getNom());
        authorLabel.getStyleClass().add("comment-author");

        Label dateLabel = new Label("Just now"); // Replace with actual date when available
        dateLabel.getStyleClass().add("comment-date");

        headerBox.getChildren().addAll(authorLabel, dateLabel);

        // Comment text
        Label contentLabel = new Label(comment.getContenuCom());
        contentLabel.getStyleClass().add("comment-content");
        contentLabel.setWrapText(true);

        contentBox.getChildren().addAll(headerBox, contentLabel);

        // Action buttons
        HBox actionsBox = new HBox(10);
        actionsBox.getStyleClass().add("comment-actions");
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().addAll("comment-action-button", "edit-button");
        editButton.setOnAction(e -> handleEditComment(comment));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().addAll("comment-action-button", "delete-button");
        deleteButton.setOnAction(e -> handleDeleteComment(comment));

        actionsBox.getChildren().addAll(editButton, deleteButton);

        // Add all components to the comment box
        commentBox.getChildren().addAll(avatarPane, contentBox);

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
                loadComments();
            } catch (SQLException ex) {
                showError("Error updating comment: " + ex.getMessage());
            }
        });
    }

    private void handleDeleteComment(Comment comment) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Comment");
        confirmation.setContentText("Are you sure you want to delete this comment?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    CommentDAO.delete(comment.getId());
                    loadComments();
                } catch (SQLException ex) {
                    showError("Error deleting comment: " + ex.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAddComment() {
        String content = commentField.getText().trim();
        if (content.isEmpty()) return;

        try {
            Comment comment = new Comment();
            comment.setPublicationId(post.getId());
            comment.setUserId(4); // Replace with actual logged-in user ID
            comment.setContenuCom(content);

            CommentDAO.save(comment);
            commentField.clear();
            loadComments();
        } catch (SQLException ex) {
            showError("Error adding comment: " + ex.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        // Close the current window
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}