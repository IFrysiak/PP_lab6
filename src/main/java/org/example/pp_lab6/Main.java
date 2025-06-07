package org.example.pp_lab6;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/start_screen.fxml"));
        primaryStage.setTitle("Aplikacja JavaFX Lab 6");
        primaryStage.getIcons().add(new Image("/logo-pwr.png"));
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
