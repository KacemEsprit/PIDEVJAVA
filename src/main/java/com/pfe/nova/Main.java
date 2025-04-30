package com.pfe.nova;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/pfe/novaview/login.fxml"));
        Scene scene = new Scene(root);
        
        // Add the modern stylesheet
        scene.getStylesheets().add(getClass().getResource("/com/pfe/novaview/modern-style.css").toExternalForm());
        
        primaryStage.setTitle("OncoKidsCare - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}