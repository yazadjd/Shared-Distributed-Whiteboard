package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseDragEvent;

import java.awt.*;
import java.net.URL;
import java.sql.Array;
import java.util.ResourceBundle;
import java.util.ArrayList;

public class Controller implements Initializable {

    ArrayList<Double> arrlistx = new ArrayList<>(10);
    ArrayList<Double> arrlisty = new ArrayList<>(10);

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

    double init_x;
    double init_y;

    String toolSelected;
    Rectangle rectangle =  new Rectangle();
    GraphicsContext brushTool;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        brushTool = canvas.getGraphicsContext2D();

        canvas.setOnMouseClicked( e -> {
            arrlistx.clear();
            arrlisty.clear();
            double size = Double.parseDouble(bsize.getText());
            init_x = e.getX() - size / 2;
            init_y = e.getY() - size / 2;
        });

        canvas.setOnMouseDragged( e -> {
            double size = Double.parseDouble(bsize.getText());
            double x = e.getX() - size / 2;
            double y = e.getY() - size / 2;
            arrlistx.add(x); arrlisty.add(y);

            if(toolSelected.matches("brush") && !bsize.getText().isEmpty()){
                brushTool.setFill(colorpicker.getValue());
                brushTool.fillRoundRect(x, y, size, size, size, size);
                //brushTool.fillRect(x,y,size,size);
            }
            if(toolSelected.matches("rectangle") && !bsize.getText().isEmpty()){
                brushTool.setFill(colorpicker.getValue());
                brushTool.fillRect(init_x,init_y,x-init_x  ,y-init_y);
                //rectangle.setRect(y, x - init_x, y - init_y);

            }
                }
        );
        canvas.setOnMouseReleased( e -> {
        //System.out.println(arrlistx);
        //System.out.println(arrlisty);
        //System.out.println(colorpicker.getValue());
        //System.out.println(bsize.getText());
        });
    }

    @FXML
    public void toolselected(ActionEvent e){
        toolSelected = "brush";
    }
    @FXML
    public void rectSelected(ActionEvent e){
        toolSelected = "rectangle";
    }
    @FXML
    private void handleButtonAction (ActionEvent event)
    {
        String messagec = chatmessage.getText();
        //operate(message_s, in, ou);
        chatmessage.setText("");
        String existingmess = textdisplay.getText();
        textdisplay.setText(existingmess + "\n\nClient 1: " + messagec);
    }

}
