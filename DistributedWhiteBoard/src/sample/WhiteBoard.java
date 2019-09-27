package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class WhiteBoard extends Application
{
    private static WhiteBoard instance;

    private Stage stage;

    public static WhiteBoard getInstance()
    {
        return instance;
    }

    public static void setInstance(WhiteBoard instance)
    {
        WhiteBoard.instance = instance;
    }

    public Stage getPrimaryStage()
    {
        return stage;
    }

    public void setPrimaryStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        setInstance(this);
        this.setPrimaryStage(stage);
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
