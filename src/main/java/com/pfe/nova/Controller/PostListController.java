package com.pfe.nova.Controller;

import com.pfe.nova.configuration.CommentReportDAO;
import com.pfe.nova.models.Post;
import com.pfe.nova.models.Comment;
import com.pfe.nova.configuration.PostDAO;
import com.pfe.nova.configuration.CommentDAO;
import com.pfe.nova.configuration.LikeDAO;
import com.pfe.nova.models.Like;
import com.pfe.nova.models.User;
import com.pfe.nova.components.ChatbotView;
import com.pfe.nova.services.EmailPostService;
import com.pfe.nova.services.EmailPostTemplateService;
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
import java.time.LocalDateTime;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import javax.sound.sampled.*;
import java.time.format.DateTimeFormatter;

public class PostListController {
    @FXML private VBox postsContainer;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ToggleButton adminModeToggle;
    @FXML private VBox adminSidebar;
    @FXML private VBox pendingPostsContainer;
    @FXML private CheckBox showPendingPosts;
    @FXML private VBox chatbotContainer;
    @FXML private ToggleButton chatbotToggleButton;
    @FXML private HBox paginationContainer;
    @FXML private Button prevPageButton;
    @FXML private Label pageNumberLabel;
    @FXML private Button nextPageButton;

    private boolean isAdminMode = false;
    private int currentUserId;
    private User currentUser;
    private boolean isChatbotInitialized = false;
    private int currentPage = 1;
    private final int pageSize = 10;
    private int totalPosts = 0;

    // Audio recording fields
    private AudioFormat audioFormat;
    private TargetDataLine line;
    private File audioFile;
    private boolean isRecording = false;
    private long startTime;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.currentUserId = user.getId();
        loadPosts();
    }

    @FXML
    public void initialize() {
        setupCategoryFilter();
        if (showPendingPosts != null) {
            showPendingPosts.selectedProperty().addListener((obs, oldVal, newVal) -> {
                currentPage = 1;
                loadPosts();
            });
        }
        if (chatbotToggleButton != null) {
            chatbotToggleButton.setText("💬 Chatbot");
            chatbotToggleButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
                chatbotToggleButton.setText(newVal ? "❌ Close Chatbot" : "💬 Chatbot");
            });
        }
        setupPaginationControls();
        // Initialize audio format
        audioFormat = new AudioFormat(44100, 16, 2, true, false);
    }

    private void setupPaginationControls() {
        if (prevPageButton != null) {
            prevPageButton.setOnAction(e -> goToPreviousPage());
        }
        if (nextPageButton != null) {
            nextPageButton.setOnAction(e -> goToNextPage());
        }
        updatePaginationControls();
    }

    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadPosts();
        }
    }

    private void goToNextPage() {
        int totalPages = (int) Math.ceil((double) totalPosts / pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            loadPosts();
        }
    }

    private void updatePaginationControls() {
        if (pageNumberLabel != null) {
            int totalPages = (int) Math.ceil((double) totalPosts / pageSize);
            pageNumberLabel.setText(String.format("Page %d of %d", currentPage, Math.max(1, totalPages)));
            prevPageButton.setDisable(currentPage <= 1);
            nextPageButton.setDisable(currentPage >= totalPages);
        }
    }

    @FXML
    private void toggleChatbot() {
        if (chatbotContainer == null || chatbotToggleButton == null) return;
        boolean isVisible = chatbotToggleButton.isSelected();
        chatbotContainer.setVisible(isVisible);
        chatbotContainer.setManaged(isVisible);
        if (isVisible && !isChatbotInitialized) {
            ChatbotView chatbotView = new ChatbotView();
            chatbotContainer.getChildren().add(chatbotView);
            isChatbotInitialized = true;
        }
    }

@FXML
public void openMessagesView() {
    try {
        System.out.println("Tentative d'ouverture de la vue Messages");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/views/messages-view.fxml"));
        System.out.println("Loader créé, tentative de chargement du FXML");
        Parent messagesView = loader.load();
        System.out.println("FXML chargé avec succès");
        
        // Récupérer le contrôleur et définir l'utilisateur actuel
        MessagesViewController controller = loader.getController();
        System.out.println("Contrôleur récupéré: " + (controller != null ? "OK" : "NULL"));
        controller.setCurrentUser(currentUser);
        System.out.println("Utilisateur défini: " + (currentUser != null ? currentUser.getId() : "NULL"));
        
        Scene scene = new Scene(messagesView);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Messages");
        stage.show();
        System.out.println("Fenêtre affichée");
    } catch (Exception e) {
        System.err.println("Erreur lors de l'ouverture de la vue Messages: " + e.getMessage());
        e.printStackTrace();
    }
}

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

    private void setupCategoryFilter() {
        categoryFilter.getItems().addAll("All", "Témoignage", "Question médicale", "Conseil", "Autre");
        categoryFilter.setValue("All");
        categoryFilter.setOnAction(e -> {
            currentPage = 1;
            loadPosts();
        });
    }

    @FXML
    private void toggleAdminMode() {
        isAdminMode = adminModeToggle.isSelected();
        if (isAdminMode) {
            switchToAdminPostsManagement();
        } else {
            currentPage = 1;
            loadPosts();
        }
    }

    @FXML
    private void setupAdminView() {
        postsContainer.getChildren().clear();
        if (adminSidebar != null) {
            adminSidebar.setVisible(true);
        }
        loadPendingPosts();
    }

    @FXML
    private void loadPendingPosts() {
        try {
            if (pendingPostsContainer != null) {
                pendingPostsContainer.getChildren().clear();
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

    public VBox createAdminPostView(Post post) {
        VBox postBox = new VBox(10);
        postBox.getStyleClass().add("admin-post-card");
        postBox.setPadding(new Insets(15));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label userLabel = new Label("User: " + post.getUser().getNom() + " " + post.getUser().getPrenom());
        userLabel.getStyleClass().add("admin-post-user");

        Label categoryLabel = new Label("Category: " + post.getCategory());
        categoryLabel.getStyleClass().add("admin-post-category");

        header.getChildren().addAll(userLabel, categoryLabel);

        Label contentLabel = new Label(post.getContent());
        contentLabel.getStyleClass().add("admin-post-content");
        contentLabel.setWrapText(true);

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

        postBox.getChildren().addAll(header, contentLabel);

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
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Approve Post");
        confirmation.setHeaderText("Approve Post");
        confirmation.setContentText("Are you sure you want to approve this post?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                post.setStatus("approved");
                PostDAO.updateStatus(post.getId(), "approved");

                // Envoyer un email au propriétaire du post avec le template HTML
                User postOwner = post.getUser();
                if (postOwner != null && postOwner.getEmail() != null) {
                    try {
                       // EmailPostService emailPostService = new EmailPostService("benalibenalirania123@gmail.com", "qwdb odbp rkgd ihuy");
                        String subject = "Votre publication a été approuvée";
                        
                        // Utiliser le template HTML
                        String htmlContent = EmailPostTemplateService.getPostApprovalTemplate(postOwner, post);
                        
                      //  emailPostService.sendHtmlEmail(postOwner.getEmail(), subject, htmlContent);
                        System.out.println("Email de notification envoyé à " + postOwner.getEmail());
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
                        // Ne pas bloquer le processus d'approbation si l'envoi d'email échoue
                    }
                }

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Post has been approved successfully!");
                success.showAndWait();

                loadPendingPosts();
            } catch (SQLException e) {
                showError("Error approving post: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleRejectPost(Post post) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Reject Post");
        confirmation.setHeaderText("Reject Post");
        confirmation.setContentText("Are you sure you want to reject this post? This will delete the post permanently.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                PostDAO.delete(post.getId());

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Post has been rejected and deleted successfully!");
                success.showAndWait();

                loadPendingPosts();
            } catch (SQLException e) {
                showError("Error rejecting post: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadPosts() {
        try {
            postsContainer.getChildren().clear();
            String selectedCategory = categoryFilter.getValue();

            if (adminSidebar != null) {
                adminSidebar.setVisible(false);
            }

            List<Post> posts;
            if (showPendingPosts != null && showPendingPosts.isSelected() && currentUser != null) {
                posts = PostDAO.findApprovedAndUserPending(currentUser.getId());
            } else if (currentUser != null && currentUser.getId() == 3) {
                posts = PostDAO.findAll();
            } else {
                posts = PostDAO.findByStatus("approved");
            }

            posts = posts.stream()
                    .filter(post -> selectedCategory == null || selectedCategory.equals("All") || selectedCategory.equals(post.getCategory()))
                    .sorted((post1, post2) -> post2.getPublishDate().compareTo(post1.getPublishDate()))
                    .toList();

            totalPosts = posts.size();

            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, posts.size());
            List<Post> postsToDisplay = startIndex < posts.size() ? posts.subList(startIndex, endIndex) : List.of();

            boolean isFirst = true;
            for (Post post : postsToDisplay) {
                if (post.getStatus().equals("pending") && currentUser != null && post.getUser().getId() == currentUser.getId()) {
                    VBox pendingNotice = createPendingPostNotice(post);
                    postsContainer.getChildren().add(pendingNotice);
                } else if (post.getStatus().equals("approved")) {
                    if (!isFirst) {
                        Separator separator = new Separator();
                        separator.getStyleClass().add("post-separator");
                        postsContainer.getChildren().add(separator);
                    }
                    postsContainer.getChildren().add(createPostView(post));
                    isFirst = false;
                }
            }

            updatePaginationControls();
        } catch (SQLException e) {
            showError("Error loading posts: " + e.getMessage());
        }
    }

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
        VBox postBox = new VBox(10);
        postBox.getStyleClass().add("post-box");
        postBox.setPadding(new Insets(15));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String authorText;
        if (post.isAnonymous()) {
            authorText = "Anonymous";
        } else if (post.getUser() != null) {
            authorText = (post.getUser().getNom() != null && post.getUser().getPrenom() != null)
                    ? post.getUser().getNom() + " " + post.getUser().getPrenom()
                    : post.getUser().getNom() != null ? post.getUser().getNom() : "Unknown User";
        } else {
            authorText = "Unknown User";
        }

        Label authorLabel = new Label("Posted by " + authorText);
        authorLabel.getStyleClass().add("post-author");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        String formattedDate = post.getPublishDate().toLocalDate().format(
                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        Label dateLabel = new Label(formattedDate);
        dateLabel.getStyleClass().add("post-date");

        Label categoryLabel = new Label(post.getCategory());
        categoryLabel.getStyleClass().add("post-category");

        header.getChildren().addAll(categoryLabel, authorLabel, headerSpacer, dateLabel);

        Label contentLabel = new Label(post.getContent());
        contentLabel.getStyleClass().add("post-content");
        contentLabel.setWrapText(true);

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

                VBox imageContainer = new VBox(imageView);
                imageContainer.getStyleClass().add("image-container");
                imageContainer.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 5; -fx-background-radius: 5;");

                imagePane.getChildren().add(imageContainer);
            } catch (Exception e) {
                System.err.println("Error loading image: " + imagePath + " - " + e.getMessage());
            }
        }

        HBox actionsBox = new HBox(20);
        actionsBox.getStyleClass().add("post-actions");
        actionsBox.setAlignment(Pos.CENTER_LEFT);

        HBox reactionBox = new HBox(15);
        reactionBox.getStyleClass().add("post-actions");
        reactionBox.setAlignment(Pos.CENTER_LEFT);
        reactionBox.setPadding(new Insets(10, 0, 10, 0));

        int likeCount = 0;
        boolean userHasLiked = false;
        try {
            likeCount = LikeDAO.countLikes(post.getId());
            userHasLiked = currentUser != null ? LikeDAO.hasUserLiked(post.getId(), currentUser.getId()) : false;
        } catch (SQLException e) {
            System.err.println("Error loading likes: " + e.getMessage());
        }

        ToggleButton likeButton = new ToggleButton();
        likeButton.getStyleClass().add("post-action-button");

        if (userHasLiked) {
            likeButton.setSelected(true);
            likeButton.setText("❤ Liked");
            likeButton.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            likeButton.setText("♡ Like");
        }

        Label likeCountLabel = new Label(likeCount + " likes");
        likeCountLabel.getStyleClass().add("post-like-count");

        likeButton.setOnAction(e -> {
            try {
                Like like = new Like();
                like.setPublicationId(post.getId());
                like.setUserId(currentUser != null ? currentUser.getId() : 0);
                like.setCreatedAt(LocalDateTime.now());

                if (likeButton.isSelected()) {
                    LikeDAO.save(like);
                    likeButton.setText("❤ Liked");
                    likeButton.setStyle("-fx-text-fill: #e74c3c;");
                    int currentLikes = Integer.parseInt(likeCountLabel.getText().split(" ")[0]);
                    likeCountLabel.setText((currentLikes + 1) + " likes");
                } else {
                    LikeDAO.delete(post.getId(), currentUser != null ? currentUser.getId() : 0, null);
                    likeButton.setText("♡ Like");
                    likeButton.setStyle("");
                    int currentLikes = Integer.parseInt(likeCountLabel.getText().split(" ")[0]);
                    if (currentLikes > 0) {
                        likeCountLabel.setText((currentLikes - 1) + " likes");
                    }
                }
            } catch (SQLException ex) {
                showError("Error updating like: " + ex.getMessage());
            }
        });

        Button commentBtn = new Button("💬 Comment");
        commentBtn.getStyleClass().add("post-action-button");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("post-action-button");

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("post-action-button");
        deleteButton.setStyle("-fx-text-fill: #e74c3c;");

        if (currentUser != null && post.getUser().getId() == currentUser.getId()) {
            editButton.setOnAction(e -> handleEditPost(post));
            deleteButton.setOnAction(e -> handleDeletePost(post));
            reactionBox.getChildren().addAll(likeButton, likeCountLabel, commentBtn, spacer, editButton, deleteButton);
        } else {
            reactionBox.getChildren().addAll(likeButton, likeCountLabel, commentBtn);
        }

        VBox commentsBox = new VBox(10);
        commentsBox.getStyleClass().add("comments-container");
        commentsBox.setPadding(new Insets(10, 0, 0, 0));

        HBox commentInput = new HBox(10);
        commentInput.setAlignment(Pos.CENTER_LEFT);
        commentInput.getStyleClass().add("comment-input");

        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment...");
        commentField.setPrefWidth(300);
        commentField.setUserData(post); // Attach post to commentField's user data
        commentField.getStyleClass().add("comment-field");
        HBox.setHgrow(commentField, Priority.ALWAYS);

        Button submitComment = new Button("Post");
        submitComment.getStyleClass().add("comment-submit-button");

        Button recordButton = new Button("🎤 Record");
        recordButton.getStyleClass().add("comment-action-button");

        Button stopButton = new Button("⬛ Stop");
        stopButton.getStyleClass().add("comment-action-button");
        stopButton.setVisible(false);
        stopButton.setManaged(false);

        // Bind the actions directly, ensuring correct button references
        recordButton.setOnAction(e -> startRecording(commentField, submitComment, recordButton, stopButton));
        stopButton.setOnAction(e -> stopRecording(commentField, submitComment, recordButton, stopButton));
        submitComment.setOnAction(e -> handleAddComment(post.getId(), commentField, null));

        commentInput.getChildren().addAll(commentField, recordButton, stopButton, submitComment);

        Label commentsLabel = new Label("Comments");
        commentsLabel.getStyleClass().add("comments-header");
        commentsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5 0;");

        commentsBox.getChildren().addAll(commentsLabel, commentInput);

        Separator separator = new Separator();
        separator.getStyleClass().add("comment-separator");
        commentsBox.getChildren().add(separator);

        try {
            List<Comment> comments = CommentDAO.findByPostId(post.getId());
            for (Comment comment : comments) {
                HBox commentBox = createCommentView(comment);
                commentsBox.getChildren().add(commentBox);
            }
        } catch (SQLException ex) {
            System.err.println("Error loading comments: " + ex.getMessage());
        }

        postBox.setOnMouseClicked(e -> {
            if (e.getTarget() != commentField && e.getTarget() != submitComment
                    && e.getTarget() != editButton && e.getTarget() != deleteButton
                    && e.getTarget() != likeButton && e.getTarget() != recordButton
                    && e.getTarget() != stopButton) {
                openPublicationDetails(post);
            }
        });

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

        if ("voice".equals(comment.getType()) && comment.getVoiceUrl() != null) {
            HBox voiceBox = new HBox(10);
            Button playButton = new Button("▶ Play");
            playButton.getStyleClass().add("comment-action-button");
            playButton.setOnAction(e -> playVoiceMessage(comment.getVoiceUrl()));
            Label durationLabel = new Label(comment.getDuration() + "s");
            durationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
            voiceBox.getChildren().addAll(playButton, durationLabel);
            commentContent.getChildren().add(voiceBox);
        } else {
            commentContent.getChildren().add(contentLabel);
        }

        if (comment.isReported()) {
            try {
                boolean isStillReported = CommentDAO.isCommentReported(comment.getId());
                if (isStillReported) {
                    Label reportedBadge = new Label("⚠ Reported");
                    reportedBadge.getStyleClass().add("reported-badge");
                    reportedBadge.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-padding: 2 5; -fx-background-radius: 3;");
                    commentContent.getChildren().add(reportedBadge);
                } else {
                    comment.setReported(false);
                }
            } catch (SQLException e) {
                System.err.println("Error checking comment report status: " + e.getMessage());
            }
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

        Button reportButton = new Button("⚠ Report");
        reportButton.getStyleClass().add("comment-action-button");
        reportButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff9800;");

        try {
            boolean hasReported = CommentReportDAO.hasUserReported(comment.getId(),
                    currentUser != null ? currentUser.getId() : 0);
            if (hasReported && comment.isReported()) {
                reportButton.setText("✓ Reported");
                reportButton.setDisable(true);
            } else if (hasReported && !comment.isReported()) {
                reportButton.setText("⚠ Report");
                reportButton.setDisable(false);
            }
        } catch (SQLException ex) {
            System.err.println("Error checking report status: " + ex.getMessage());
        }

        reportButton.setOnAction(e -> handleReportComment(comment));

        if (currentUser != null && comment.getUserId() == currentUser.getId()) {
            buttonsBox.getChildren().addAll(editButton, deleteButton);
        } else {
            buttonsBox.getChildren().add(reportButton);
        }

        commentBox.getChildren().addAll(avatarPlaceholder, commentContent, spacer, buttonsBox);

        return commentBox;
    }

    private void startRecording(TextField commentField, Button submitButton, Button recordButton, Button stopButton) {
        if (isRecording) return;
        isRecording = true;

        // Disable text input and submit button during recording
        commentField.setDisable(true);
        submitButton.setDisable(true);
        recordButton.setVisible(false);
        recordButton.setManaged(false);
        stopButton.setVisible(true);
        stopButton.setManaged(true);

        // Create directory if it doesn't exist
        File audioDir = new File("audio/comments");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }

        // Generate unique filename
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        audioFile = new File(audioDir, "comment_" + timestamp + ".wav");

        try {
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
            stopButton.setVisible(false);
            stopButton.setManaged(false);
        }
    }

    private void stopRecording(TextField commentField, Button submitButton, Button recordButton, Button stopButton) {
        if (!isRecording) return;
        isRecording = false;

        // Re-enable text input and submit button
        commentField.setDisable(false);
        submitButton.setDisable(false);
        recordButton.setVisible(true);
        recordButton.setManaged(true);
        stopButton.setVisible(false);
        stopButton.setManaged(false);

        // Stop and close the recording line
        line.stop();
        line.close();

        // Calculate duration
        long endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - startTime) / 1000);

        // Update comment field to indicate voice message
        commentField.setText("[Voice Message]");

        // Store audio file path and duration for submission
        Post post = (Post) commentField.getUserData(); // Retrieve post from user data
        submitButton.setOnAction(e -> handleAddComment(
                post.getId(),
                commentField,
                new CommentData(audioFile.getAbsolutePath(), duration)
        ));
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

    private void handleReportComment(Comment comment) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Report Comment");
        dialog.setHeaderText("Why are you reporting this comment?");

        ButtonType reportButtonType = new ButtonType("Report", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reportButtonType, ButtonType.CANCEL);

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

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(rb1, rb2, rb3, rb4);
        vbox.setPadding(new Insets(20, 10, 10, 10));

        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reportButtonType) {
                return group.getSelectedToggle().getUserData().toString();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(reason -> {
            try {
                CommentReportDAO.reportComment(comment.getId(),
                        currentUser != null ? currentUser.getId() : 0, reason);

                comment.setReported(true);
                comment.setReportReason(reason);
                CommentDAO.updateReportStatus(comment.getId(), true, reason);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Comment Reported");
                alert.setHeaderText(null);
                alert.setContentText("Thank you for your report. Our moderators will review it.");
                alert.showAndWait();

                loadPosts();
            } catch (SQLException ex) {
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
                loadPosts();
            } catch (SQLException ex) {
                showError("Error updating comment: " + ex.getMessage());
            }
        });
    }

    private void handleAddComment(int postId, TextField commentField, CommentData commentData) {
        String content = commentField.getText().trim();
        if (content.isEmpty() && commentData == null) return;

        try {
            Comment comment = new Comment();
            comment.setPublicationId(postId);
            comment.setUserId(currentUser != null ? currentUser.getId() : 0);
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
            loadPosts();
        } catch (SQLException ex) {
            showError("Error adding comment: " + ex.getMessage());
        }
    }

    private void handleDeleteComment(Comment comment) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Comment");
        confirmation.setHeaderText("Delete Comment");
        confirmation.setContentText("Are you sure you want to delete this comment? This action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CommentDAO.delete(comment.getId());

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Comment deleted successfully!");
                success.showAndWait();

                loadPosts();
            } catch (SQLException e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Error deleting comment");
                error.setContentText("An error occurred: " + e.getMessage());
                error.showAndWait();
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void closeChatbot() {
        if (chatbotToggleButton != null) {
            chatbotToggleButton.setSelected(false);
            chatbotContainer.setVisible(false);
            chatbotContainer.setManaged(false);
        }
    }

    @FXML
    private void handleNewPost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-form.fxml"));
            Parent root = loader.load();

            PostFormController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Create New Post");

            Scene scene = new Scene(root, 700, 600);
            stage.setScene(scene);

            stage.setMinWidth(500);
            stage.setMinHeight(400);

            stage.showAndWait();

            loadPosts();
        } catch (IOException e) {
            showError("Error opening post form: " + e.getMessage());
        }
    }

    private void handleEditPost(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/post-form.fxml"));
            Parent root = loader.load();

            PostFormController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setEditMode(post);

            Stage stage = new Stage();

            Scene scene = new Scene(root, 700, 600);
            stage.setScene(scene);

            stage.setMinWidth(500);
            stage.setMinHeight(400);

            stage.setTitle("Edit Post");
            stage.showAndWait();

            loadPosts();
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
                    loadPosts();
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

    public void setUserMode() {
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
            controller.setCurrentUser(currentUser);

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
}