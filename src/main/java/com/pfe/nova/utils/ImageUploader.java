package com.pfe.nova.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ImageUploader {
    
    // The base directory where images will be stored on the server
    private static final String SERVER_IMAGE_DIR = "D:/xampp12/htdocs/img/";
    
    // The URL prefix for accessing the images
    private static final String IMAGE_URL_PREFIX = "http://127.0.0.1/img/";
    
    /**
     * Uploads an image to the server and returns the URL to access it
     * 
     * @param imageFile The image file to upload
     * @return The URL to access the uploaded image
     * @throws IOException If there's an error during the upload
     */
    public static String uploadImage(File imageFile) throws IOException {
        // Create the directory if it doesn't exist
        File directory = new File(SERVER_IMAGE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Generate a unique filename to avoid collisions
        String originalFilename = imageFile.getName();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Copy the file to the server directory
        Path sourcePath = imageFile.toPath();
        Path targetPath = Paths.get(SERVER_IMAGE_DIR + uniqueFilename);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return the URL to access the image
        return IMAGE_URL_PREFIX + uniqueFilename;
    }
}