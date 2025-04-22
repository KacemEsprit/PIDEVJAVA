package com.pfe.nova;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/pfe/novaview/main-view.fxml"));
        
        // Create scene with a reasonable size
        Scene scene = new Scene(root, 1000, 700);
        
        // Add the modern stylesheet
        scene.getStylesheets().add(getClass().getResource("/com/pfe/novaview/modern-style.css").toExternalForm());
        
        primaryStage.setTitle("OncoKidsCare");
        primaryStage.setScene(scene);
        
        // Set minimum size constraints to prevent too small windows
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}