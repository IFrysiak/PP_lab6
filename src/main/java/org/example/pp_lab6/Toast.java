package org.example.pp_lab6;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toast {
    public static void show(String message) {
        Popup popup = new Popup();
        Label label = new Label(message);
        label.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 5;");
        popup.getContent().add(label);
        Stage stage = Stage.getWindows().stream().filter(Window -> Window.isShowing()).findFirst().map(Window -> (Stage) Window).orElse(null);
        if (stage != null) {
            popup.show(stage);
            new Timeline(new KeyFrame(Duration.seconds(2), e -> popup.hide())).play();
        }
    }
}
