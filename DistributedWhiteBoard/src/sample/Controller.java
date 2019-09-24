package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseDragEvent;

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

    boolean toolSelected = false;

    GraphicsContext brushTool;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        brushTool = canvas.getGraphicsContext2D();

        canvas.setOnMouseClicked( e -> {
            arrlistx.clear();
            arrlisty.clear();
        });

        canvas.setOnMouseDragged( e -> {
            double size = Double.parseDouble(bsize.getText());
            double x = e.getX() - size / 2;
            double y = e.getY() - size / 2;
            arrlistx.add(x); arrlisty.add(y);

            if(toolSelected && !bsize.getText().isEmpty()){
                brushTool.setFill(colorpicker.getValue());
                brushTool.fillRoundRect(x, y, size, size, size, size);
                //brushTool.fillRect(x,y,size,size);
            }
                }
        );
        canvas.setOnMouseReleased( e -> {
        System.out.println(arrlistx);
        System.out.println(arrlisty);
        System.out.println(colorpicker.getValue());
        System.out.println(bsize.getText());
        });
    }

    @FXML
    public void toolselected(ActionEvent e){
        toolSelected = true;
    }

}
