package com.pfe.nova.Controller;

import com.pfe.nova.models.Post;
import com.pfe.nova.models.User;
import com.pfe.nova.configuration.PostDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
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
                updateImageCount();
            }
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
        selectedImagePaths.clear();
        selectedImagePaths.addAll(post.getImageUrls());
        updateImageCount();
    }

    // In your method where you save the post and its images
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
            
            // Remove this block that uses DEFAULT_USER_ID
            /*
            if (editingPost == null) {
                User tempUser = new User();
                tempUser.setId(DEFAULT_USER_ID); // Use the constant defined above
                post.setUser(tempUser);
            }
            */
            
            // Upload images and get their URLs
            List<String> imageUrls = new ArrayList<>();
            for (String imagePath : selectedImagePaths) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    String imageUrl = ImageUploader.uploadImage(imageFile);
                    imageUrls.add(imageUrl);
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