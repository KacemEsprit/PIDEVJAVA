package com.pfe.nova.Controller;

import com.pfe.nova.models.Post;
import com.pfe.nova.models.User;
import com.pfe.nova.configuration.PostDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Import the ImageUploader
import com.pfe.nova.utils.ImageUploader;

public class PostFormController {
    @FXML private TextArea contentArea;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private CheckBox anonymousCheckBox;
    @FXML private Button addImagesButton;
    @FXML private Label imageCountLabel;
    @FXML private FlowPane imagePreviewPane;
    @FXML private Label errorLabel;

    // Remove the hardcoded user ID constant if it exists
    // private static final int DEFAULT_USER_ID = 2;
    
    // Add a field for the current user
    private User currentUser;

    private List<String> selectedImagePaths = new ArrayList<>();
    private Post editingPost = null;

    // Add this method to set the current user
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        setupCategoryComboBox();
        setupImageButton();
        
        // Add character limit to content area
        setupContentAreaWithLimit();
    }

    private void setupContentAreaWithLimit() {
      
        final int MAX_CHARS = 500;
        
        // Add a label to show character count
        Label charCountLabel = new Label("0/" + MAX_CHARS);
        charCountLabel.setStyle("-fx-text-fill: #757575;");
        
        // Add the label below the content area
        if (contentArea.getParent() instanceof VBox) {
            VBox parent = (VBox) contentArea.getParent();
            int index = parent.getChildren().indexOf(contentArea);
            parent.getChildren().add(index + 1, charCountLabel);
        }
        
        // Add listener to update character count and limit input
        contentArea.textProperty().addListener((observable, oldValue, newValue) -> {
            int currentLength = newValue.length();
            
            // Update the character count label
            charCountLabel.setText(currentLength + "/" + MAX_CHARS);
            
            // Change color when approaching the limit
            if (currentLength > MAX_CHARS * 0.8) {
                charCountLabel.setStyle("-fx-text-fill: #e67e22;"); // Orange when approaching limit
            } else {
                charCountLabel.setStyle("-fx-text-fill: #757575;"); // Default color
            }
            
            // Truncate text if it exceeds the limit
            if (currentLength > MAX_CHARS) {
                contentArea.setText(newValue.substring(0, MAX_CHARS));
                charCountLabel.setText(MAX_CHARS + "/" + MAX_CHARS);
                charCountLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red when at limit
            }
        });
    }

    private void setupCategoryComboBox() {
        categoryComboBox.getItems().addAll(
            "Témoignage",
            "Question médicale",
            "Conseil",
            "Autre"
        );
    }

    private void setupImageButton() {
        addImagesButton.setOnAction(e -> handleImageSelection());
    }

    @FXML
    private void handleSave() {
        savePost();
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void handleImageSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.gif")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(addImagesButton.getScene().getWindow());
        if (files != null) {
            for (File file : files) {
                if (selectedImagePaths.size() >= 5) break;
                selectedImagePaths.add(file.getAbsolutePath());
            }
            updateImageCount();
            updateImagePreviews(); // Update the previews after selection
        }
    }

    private void updateImageCount() {
        imageCountLabel.setText(selectedImagePaths.size() + "/5 images");
    }

    private void closeForm() {
        addImagesButton.getScene().getWindow().hide();
    }

    public void setEditMode(Post post) {
        this.editingPost = post;
        contentArea.setText(post.getContent());
        categoryComboBox.setValue(post.getCategory());
        anonymousCheckBox.setSelected(post.isAnonymous());
        
        // Clear any previously selected images
        selectedImagePaths.clear();
        
        // Add all existing image URLs from the post
        if (post.getImageUrls() != null) {
            selectedImagePaths.addAll(post.getImageUrls());
        }
        
        // Update the image count display
        updateImageCount();
        
        // Display image previews
        updateImagePreviews();
    }
    
    // Add this new method to display image previews with remove buttons
    private void updateImagePreviews() {
        // Clear existing previews
        imagePreviewPane.getChildren().clear();
        
        // Add preview for each image
        for (int i = 0; i < selectedImagePaths.size(); i++) {
            final int index = i;
            String imagePath = selectedImagePaths.get(i);
            
            try {
                // Create a container for each image and its remove button
                javafx.scene.layout.VBox imageContainer = new javafx.scene.layout.VBox(5);
                imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
                
                // Load the image
                javafx.scene.image.Image image;
                if (imagePath.startsWith("http")) {
                    image = new javafx.scene.image.Image(imagePath, 100, 100, true, true);
                } else {
                    image = new javafx.scene.image.Image(new File(imagePath).toURI().toString(), 100, 100, true, true);
                }
                
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
                imageView.setFitHeight(80);
                imageView.setFitWidth(80);
                imageView.setPreserveRatio(true);
                
                // Create remove button
                Button removeButton = new Button("Remove");
                removeButton.getStyleClass().add("remove-image-button");
                removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");
                removeButton.setOnAction(e -> {
                    selectedImagePaths.remove(index);
                    updateImageCount();
                    updateImagePreviews();
                });
                
                // Add image and button to container
                imageContainer.getChildren().addAll(imageView, removeButton);
                
                // Add the container to the preview pane
                imagePreviewPane.getChildren().add(imageContainer);
            } catch (Exception e) {
                System.err.println("Error loading image preview: " + e.getMessage());
            }
        }
        
        // Add a "Remove All" button if there are images
        if (!selectedImagePaths.isEmpty()) {
            Button removeAllButton = new Button("Remove All Images");
            removeAllButton.getStyleClass().add("remove-all-button");
            removeAllButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            removeAllButton.setOnAction(e -> {
                selectedImagePaths.clear();
                updateImageCount();
                updateImagePreviews();
            });
            
            // Create a container for the remove all button
            javafx.scene.layout.HBox buttonContainer = new javafx.scene.layout.HBox();
            buttonContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            buttonContainer.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
            buttonContainer.getChildren().add(removeAllButton);
            
            // Add the button container to the preview pane
            imagePreviewPane.getChildren().add(buttonContainer);
        }
    }
    
    private void savePost() {
        try {
            // Validate that we have a current user
            if (currentUser == null) {
                errorLabel.setText("Error: No user logged in");
                errorLabel.setVisible(true);
                return;
            }
            
            // Create and set up the post object
            Post post = (editingPost != null) ? editingPost : new Post();
            post.setContent(contentArea.getText());
            post.setCategory(categoryComboBox.getValue());
            post.setAnonymous(anonymousCheckBox.isSelected());
            
            // Use the current user instead of hardcoded ID
            if (editingPost == null) {
                post.setUser(currentUser);
            }
            
            // Only upload new images if they're not already URLs
            List<String> imageUrls = new ArrayList<>();
            for (String imagePath : selectedImagePaths) {
                // If it's already a URL (from previous upload), keep it as is
                if (imagePath.startsWith("http")) {
                    imageUrls.add(imagePath);
                } else {
                    // Otherwise, upload the new image
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        String imageUrl = ImageUploader.uploadImage(imageFile);
                        imageUrls.add(imageUrl);
                    }
                }
            }
            post.setImageUrls(imageUrls);
            
            // Save the post to the database
            PostDAO.save(post);
            
            // Close the form
            closeForm();
        } catch (Exception e) {
            errorLabel.setText("Error saving post: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
}