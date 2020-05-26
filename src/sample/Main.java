package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    private static Scene mainScene;

    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        stage.setTitle("PaintAway");

        mainScene = new Scene(root, 640, 480);
        mainScene.getStylesheets().add("css.css");
        stage.setScene(mainScene);
        stage.getIcons().add(new Image("file:paintsave.png"));
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
