package com.pfe.nova.Controller;

import com.pfe.nova.configuration.PostDAO;
import com.pfe.nova.models.Post;
import com.pfe.nova.models.Comment;
import com.pfe.nova.configuration.CommentDAO;
import com.pfe.nova.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import com.pfe.nova.configuration.LikeDAO;
import com.pfe.nova.models.Like;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.io.File;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.net.URL;
import javafx.application.Platform;
import javax.sound.sampled.*;
import java.time.format.DateTimeFormatter;

public class PublicationDetailsController {
    @FXML private VBox publicationContent;
    @FXML private VBox commentsList;
    @FXML private TextField commentField;
    @FXML private Button backButton;

    private Post post;
    private User currentUser;

    // Audio recording fields
    private AudioFormat audioFormat;
    private TargetDataLine line;
    private File audioFile;
    private boolean isRecording = false;
    private long startTime;

    public void setPost(Post post) {
        this.post = post;
        displayPost();
        loadComments();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void displayPost() {
        publicationContent.getChildren().clear();

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("post-header");

        Label categoryLabel = new Label(post.getCategory());
        categoryLabel.getStyleClass().add("publication-category");

        Label authorLabel = new Label(post.isAnonymous() ? "Anonymous" :
                (post.getUser() != null ? post.getUser().getNom() + " " + post.getUser().getPrenom() : "Unknown User"));
        authorLabel.getStyleClass().add("publication-author");

        String formattedDate = post.getPublishDate().toLocalDate().format(
                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        Label dateLabel = new Label("Posted on " + formattedDate);
        dateLabel.getStyleClass().add("publication-date");

        VBox authorInfo = new VBox(5);
        authorInfo.getChildren().addAll(authorLabel, dateLabel);

        headerBox.getChildren().addAll(categoryLabel, authorInfo);

        Label contentLabel = new Label(post.getContent());
        contentLabel.getStyleClass().add("publication-text");
        contentLabel.setWrapText(true);

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

        publicationContent.getChildren().addAll(headerBox, contentLabel);
        if (!post.getImageUrls().isEmpty()) {
            publicationContent.getChildren().add(imagesPane);
        }
    }

    private int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : 0;
    }

    private void loadComments() {
        try {
            commentsList.getChildren().clear();

            Label commentsTitle = new Label("Comments");
            commentsTitle.getStyleClass().add("section-title");

            HBox commentInputBox = new HBox(10);
            commentInputBox.setAlignment(Pos.CENTER_LEFT);
            commentInputBox.getStyleClass().add("comment-input-container");

            commentField.getStyleClass().add("comment-input-field");
            commentField.setPromptText("Write a comment...");
            commentField.setUserData(post); // Attach post to commentField's user data
            HBox.setHgrow(commentField, Priority.ALWAYS);

            Button submitButton = new Button("Post");
            submitButton.getStyleClass().add("comment-submit-button");

            Button recordButton = new Button("🎤 Record");
            recordButton.getStyleClass().add("comment-action-button");
            recordButton.setOnAction(e -> startRecording(commentField, submitButton, recordButton));

            Button stopButton = new Button("⬛ Stop");
            stopButton.getStyleClass().add("comment-action-button");
            stopButton.setVisible(false);
            stopButton.setManaged(false);
            stopButton.setOnAction(e -> stopRecording(commentField, submitButton, recordButton, stopButton));

            submitButton.setOnAction(e -> handleAddComment(null));

            commentInputBox.getChildren().addAll(commentField, recordButton, stopButton, submitButton);

            Separator

                    separator = new Separator();
            separator.getStyleClass().add("comments-separator");

            VBox commentsContainer = new VBox(15);
            commentsContainer.getStyleClass().add("comments-list");

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

            commentsList.getChildren().addAll(commentsTitle, commentInputBox, separator, commentsContainer);
        } catch (SQLException ex) {
            showError("Error loading comments: " + ex.getMessage());
        }
    }

    private HBox createCommentView(Comment comment) {
        HBox commentBox = new HBox(15);
        commentBox.getStyleClass().add("comment-box");

        StackPane avatarPane = new StackPane();
        avatarPane.getStyleClass().add("comment-avatar");

        Text initial = new Text(comment.getUser().getNom().substring(0, 1).toUpperCase());
        initial.setFill(Color.WHITE);
        initial.setStyle("-fx-font-weight: bold;");

        avatarPane.getChildren().add(initial);

        VBox contentBox = new VBox(5);
        contentBox.getStyleClass().add("comment-content-box");
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        HBox headerBox = new HBox(10);
        headerBox.getStyleClass().add("comment-header");

        Label authorLabel = new Label(comment.getUser().getNom());
        authorLabel.getStyleClass().add("comment-author");

        Label dateLabel = new Label("Just now");
        dateLabel.getStyleClass().add("comment-date");

        headerBox.getChildren().addAll(authorLabel, dateLabel);

        Label contentLabel = new Label(comment.getContenuCom());
        contentLabel.getStyleClass().add("comment-content");
        contentLabel.setWrapText(true);

        if ("voice".equals(comment.getType()) && comment.getVoiceUrl() != null) {
            HBox voiceBox = new HBox(10);
            Button playButton = new Button("▶ Play");
            playButton.getStyleClass().add("comment-action-button");
            playButton.setOnAction(e -> playVoiceMessage(comment.getVoiceUrl()));
            Label durationLabel = new Label(comment.getDuration() + "s");
            durationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
            voiceBox.getChildren().addAll(playButton, durationLabel);
            contentBox.getChildren().add(voiceBox);
        } else {
            contentBox.getChildren().add(contentLabel);
        }

        HBox actionsBox = new HBox(10);
        actionsBox.getStyleClass().add("comment-actions");
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().addAll("comment-action-button", "edit-button");
        editButton.setOnAction(e -> handleEditComment(comment));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().addAll("comment-action-button", "delete-button");
        deleteButton.setOnAction(e -> handleDeleteComment(comment));

        if (currentUser != null && comment.getUserId() == currentUser.getId()) {
            actionsBox.getChildren().addAll(editButton, deleteButton);
        }

        commentBox.getChildren().addAll(avatarPane, contentBox, actionsBox);

        return commentBox;
    }

    private void startRecording(TextField commentField, Button submitButton, Button recordButton) {
        if (isRecording) return;
        isRecording = true;

        commentField.setDisable(true);
        submitButton.setDisable(true);
        recordButton.setVisible(false);
        recordButton.setManaged(false);

        // Ensure stopButton is accessible
        Button stopButton = (Button) commentField.getParent().getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof Button && "⬛ Stop".equals(((Button) node).getText()))
                .findFirst()
                .orElse(null);
        if (stopButton != null) {
            stopButton.setVisible(true);
            stopButton.setManaged(true);
        }

        File audioDir = new File("audio/comments");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        audioFile = new File(audioDir, "comment_" + timestamp + ".wav");

        try {
            audioFormat = new AudioFormat(44100, 16, 2, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();
            startTime = System.currentTimeMillis();

            new Thread(() -> {
                try (AudioInputStream ais = new AudioInputStream(line)) {
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, audioFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error recording audio: " + e.getMessage());
                }
            }).start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            showError("Microphone not available: " + e.getMessage());
            isRecording = false;
            commentField.setDisable(false);
            submitButton.setDisable(false);
            recordButton.setVisible(true);
            recordButton.setManaged(true);
            if (stopButton != null) {
                stopButton.setVisible(false);
                stopButton.setManaged(false);
            }
        }
    }

    private void stopRecording(TextField commentField, Button submitButton, Button recordButton, Button stopButton) {
        if (!isRecording) return;
        isRecording = false;

        commentField.setDisable(false);
        submitButton.setDisable(false);
        recordButton.setVisible(true);
        recordButton.setManaged(true);
        stopButton.setVisible(false);
        stopButton.setManaged(false);

        line.stop();
        line.close();

        long endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - startTime) / 1000);

        commentField.setText("[Voice Message]");

        submitButton.setOnAction(e -> handleAddComment(new CommentData(audioFile.getAbsolutePath(), duration)));
    }

    private void playVoiceMessage(String voiceUrl) {
        try {
            File soundFile = new File(voiceUrl);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error playing voice message: " + e.getMessage());
        }
    }

    private class CommentData {
        String voiceUrl;
        int duration;

        CommentData(String voiceUrl, int duration) {
            this.voiceUrl = voiceUrl;
            this.duration = duration;
        }
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

    private void handleAddComment(CommentData commentData) {
        String content = commentField.getText().trim();
        if (content.isEmpty() && commentData == null) return;

        try {
            Comment comment = new Comment();
            comment.setPublicationId(post.getId());
            comment.setUserId(getCurrentUserId());
            if (commentData != null) {
                comment.setType("voice");
                comment.setContenuCom("[Voice Message]");
                comment.setVoiceUrl(commentData.voiceUrl);
                comment.setDuration(commentData.duration);
            } else {
                comment.setType("text");
                comment.setContenuCom(content);
            }

            CommentDAO.save(comment);
            commentField.clear();
            loadComments();
        } catch (SQLException ex) {
            showError("Error adding comment: " + ex.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    public void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setOnShown(event -> refreshPost());
        });
    }

    public void refreshPost() {
        try {
            Post refreshedPost = PostDAO.getPostById(post.getId());
            if (refreshedPost != null) {
                this.post = refreshedPost;
                displayPost();
                loadComments();
            }
        } catch (SQLException e) {
            showError("Error refreshing post: " + e.getMessage());
        }
    }
}