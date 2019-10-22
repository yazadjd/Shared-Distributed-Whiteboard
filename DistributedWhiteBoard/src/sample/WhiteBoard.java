package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
        stage.setScene(new Scene(root, 1280, 720, Color.WHITE));
        stage.show();
        stage.setOnCloseRequest( e -> {
            e.consume();
            closeProgram();
        });
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    private void closeProgram(){
        System.exit(0);
    }
}
