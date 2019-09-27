package sample;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Array;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ArrayList;

public class Controller implements Initializable
{

    ArrayList<Double> arrlistx = new ArrayList<>(1000);
    ArrayList<Double> arrlisty = new ArrayList<>(1000);

    @FXML
    private ColorPicker colorpicker;

    @FXML
    private TextField bsize;

    @FXML
    private Canvas canvas;

    @FXML
    private Label textdisplay;

    @FXML
    private TextArea chatmessage;

    @FXML
    private Button sendbutton;

    @FXML
    private MenuItem newoption;

    @FXML
    private MenuItem openoption;

    @FXML
    private MenuItem saveoption;

    @FXML
    private MenuItem saveasoption;

    @FXML
    private MenuItem closeoption;

    private double init_x = 0;
    private double init_y = 0;
    private double x;
    private double y;
    private double size;
    private String toolSelected;
    private GraphicsContext brushTool;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

        brushTool = canvas.getGraphicsContext2D();
        //brushTool.setLineWidth(1);
        toolSelected = "brush"; // default tool selected
        bsize.setText("10"); //Default size
        colorpicker.setValue(Color.BLACK); //Default color

        canvas.setOnMousePressed( e -> {
            brushTool.setFill(colorpicker.getValue());
            size = Double.parseDouble(bsize.getText());
            arrlistx.clear();
            arrlisty.clear();
            double size = Double.parseDouble(bsize.getText());
            init_x = e.getX() - size / 2;
            init_y = e.getY() - size / 2;
        });

        canvas.setOnMouseDragged( e -> {

            x = e.getX() - size / 2;
            y = e.getY() - size / 2;
            if (toolSelected.matches("brush")) {
                arrlistx.add(x);
                arrlisty.add(y);
            }

            if(toolSelected.matches("eraser") && !bsize.getText().isEmpty()){
                brushTool.setFill(Paint.valueOf("#f2f2f2"));
                brushTool.fillRoundRect(x, y, size, size, size, size);
            }

            if(toolSelected.matches("brush") && !bsize.getText().isEmpty()){
                brushTool.setFill(colorpicker.getValue());
                brushTool.fillRoundRect(x, y, size, size, size, size);
            }
            if(toolSelected.matches("rectangle")){
                brushTool.setFill(colorpicker.getValue());
                if((x - init_x ) < 0) {
                    if ((y - init_y) < 0 ){
                        brushTool.fillRect(x, y, init_x-x, init_y - y);
                    }
                    else {
                        brushTool.fillRect(x, init_y, init_x-x, y - init_y);
                    }
                }
                else if ((x - init_x) > 0){
                    if ((y - init_y) > 0) {
                        brushTool.fillRect(init_x, init_y,x - init_x  ,y - init_y);
                    }
                    else {
                        brushTool.fillRect(init_x, y, x - init_x, init_y - y);
                    }

                }
            }
        });

        canvas.setOnMouseReleased( e -> {

            if (toolSelected.matches("circle")) {
                //brushTool.clearRect(init_x - 1, init_y - 1,(x - init_x + y - init_y)/2, (x - init_x + y - init_y)/2);
                //brushTool.fillOval(init_x, init_y, (x - init_x + y - init_y)/2, (x - init_x + y - init_y)/2);
                if((x - init_x ) < 0) {
                    if ((y - init_y) < 0 ){
                        brushTool.fillOval(x, y, (init_x-x + init_y-y), (init_x-x + init_y-y));
                        arrlistx.add(x);
                        arrlistx.add((init_x-x + init_y-y)/2);
                        arrlisty.add(y);
                        arrlisty.add((init_x-x + init_y-y)/2);
                    }
                    else {
                        brushTool.fillOval(x, y, (init_x-x + y - init_y)/2, (init_x-x + y - init_y)/2);
                        arrlistx.add(x);
                        arrlistx.add((init_x-x + y - init_y)/2);
                        arrlisty.add(y);
                        arrlisty.add((init_x-x + y - init_y)/2);
                    }
                }
                else if ((x - init_x) > 0) {
                    if ((y - init_y) > 0) {
                        brushTool.fillOval(init_x, init_y, (x - init_x + y - init_y) / 2, (x - init_x + y - init_y) / 2);
                        arrlistx.add(init_x);
                        arrlistx.add((x - init_x + y - init_y) / 2);
                        arrlisty.add(init_y);
                        arrlisty.add((x - init_x + y - init_y) / 2);
                    }
                    else {
                        brushTool.fillOval(init_x, init_y, (x - init_x + init_y - y) / 2, (x - init_x + init_y - y) / 2);
                        arrlistx.add(init_x);
                        arrlistx.add((x - init_x + init_y - y) / 2);
                        arrlisty.add(init_y);
                        arrlisty.add((x - init_x + init_y - y) / 2);
                    }
                }
            }
            if(toolSelected.matches("line") && !bsize.getText().isEmpty()){
                brushTool.setStroke(colorpicker.getValue());
                brushTool.setLineWidth(size);
                brushTool.strokeLine(init_x,init_y, x, y);
                arrlistx.add(init_x);
                arrlistx.add(x);
                arrlisty.add(init_y);
                arrlisty.add(y);
            }
            if (toolSelected.matches("rectangle"))
            {
                arrlistx.add(init_x);
                arrlisty.add(init_y);
                arrlistx.add(x-init_x);
                arrlisty.add(y - init_y);
            }
            if (toolSelected.matches("oval")) {
                if((x - init_x ) < 0) {
                    if ((y - init_y) < 0 ){
                        brushTool.fillOval(x, y, init_x - x, init_y - y);
                        arrlistx.add(x);
                        arrlistx.add(init_x-x);
                        arrlisty.add(y);
                        arrlisty.add(init_y-y);
                    }
                    else {
                        brushTool.fillOval(x, y, init_x - x , y - init_y);
                        arrlistx.add(x);
                        arrlistx.add(init_x-x);
                        arrlisty.add(y);
                        arrlisty.add(y - init_y);
                    }
                }
                else if ((x - init_x) > 0) {
                    if ((y - init_y) > 0) {
                        brushTool.fillOval(init_x, init_y, x - init_x, y - init_y);
                        arrlistx.add(init_x);
                        arrlistx.add(x - init_x);
                        arrlisty.add(init_y);
                        arrlisty.add(y - init_y);
                    }
                    else {
                        brushTool.fillOval(init_x, init_y, x - init_x , init_y - y);
                        arrlistx.add(init_x);
                        arrlistx.add(x - init_x);
                        arrlisty.add(init_y);
                        arrlisty.add(init_y - y);
                    }
                }

            }
            System.out.println("Arr list x = " + arrlistx);
            System.out.println("Arr list y = " + arrlisty);
            System.out.println("Color = " + colorpicker.getValue());
            System.out.println("Brush Size = " + bsize.getText());
        });
    }

    @FXML
    public void toolselected(ActionEvent e)
    {
        toolSelected = "brush";
    }

    @FXML
    public void rectSelected(ActionEvent e)
    {
        toolSelected = "rectangle";
    }

    @FXML
    public void circleSelected(ActionEvent e)
    {
        toolSelected = "circle";
    }

    @FXML
    public void ovalSelected(ActionEvent e)
    {
        toolSelected = "oval";
    }

    @FXML
    public void eraserSelected(ActionEvent e)
    {
        toolSelected = "eraser";
    }

    @FXML
    public void lineSelected(ActionEvent e)
    {
        toolSelected = "line";
    }

    @FXML
    public void handleButtonAction (ActionEvent event)
    {
        String messagec = chatmessage.getText();
        //operate(message_s, in, ou);
        chatmessage.setText("");
        String existingmess = textdisplay.getText();
        textdisplay.setText(existingmess + "\n\nClient 1: " + messagec);
    }

    @FXML
    public void clicknew (ActionEvent event)
    {
        System.exit(0);
    }

    @FXML
    public void clickopen (ActionEvent event)
    {
        System.exit(0);
    }

    @FXML
    public void clicksaveas (ActionEvent event) //Saves the current session with a user-defined name
    {
        FileChooser savefile = new FileChooser();
        savefile.setTitle("Save File As...");
        File file = savefile.showSaveDialog(WhiteBoard.getInstance().getPrimaryStage());
        if (file != null)
        {
            try
            {
                Image snapshot = canvas.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                new Alert(Alert.AlertType.INFORMATION, "Your file has been saved successfully.").show();
            }
            catch (IOException e)
            {
                //e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Unable to save image: " + e).show();
                //System.out.println("Unable to save image: " + e);
            }
        }
    }

    @FXML
    public void clicksave (ActionEvent event) //Saves the current session by default as saved_session.png
    {
        try
        {
            Image snapshot = canvas.snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File("saved_session.png"));
            new Alert(Alert.AlertType.INFORMATION, "Your file has been saved successfully.").show();
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Unable to save image: " + e).show();
            //System.out.println("Unable to save image: " + e);
        }
    }

    @FXML
    public void clickclose (ActionEvent event) //Closes the current session
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Close Whiteboard");
        alert.setHeaderText("Are you sure you want to exit the Whiteboard?");
        alert.setContentText("Unsaved changes will be lost.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            Platform.exit();
        }
    }
}