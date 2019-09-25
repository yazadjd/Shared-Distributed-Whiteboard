package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WhiteBoard extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        stage.setTitle("White Board");
        stage.setScene(new Scene(root, 1280, 720));
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
