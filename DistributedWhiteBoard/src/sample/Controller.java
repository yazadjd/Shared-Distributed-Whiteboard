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

    double init_x = 0;
    double init_y = 0;
    double x;
    double y;
    String toolSelected;
    GraphicsContext brushTool;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        brushTool = canvas.getGraphicsContext2D();

        canvas.setOnMousePressed( e -> {
            arrlistx.clear();
            arrlisty.clear();
            double size = Double.parseDouble(bsize.getText());
            init_x = e.getX() - size / 2;
            init_y = e.getY() - size / 2;
        });

        canvas.setOnMouseDragged( e -> {
            double size = Double.parseDouble(bsize.getText());
            x = e.getX() - size / 2;
            y = e.getY() - size / 2;
            if (toolSelected.matches("brush")) {
                arrlistx.add(x);
                arrlisty.add(y);
            }

            if(toolSelected.matches("brush") && !bsize.getText().isEmpty()){
                brushTool.setFill(colorpicker.getValue());
                brushTool.fillRoundRect(x, y, size, size, size, size);
            }
            if(toolSelected.matches("rectangle") && !bsize.getText().isEmpty()){
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
            if (toolSelected.matches("rectangle")) {
                arrlistx.add(init_x);
                arrlisty.add(init_y);
                arrlistx.add(x-init_x);
                arrlisty.add(y - init_y);
            }
        System.out.println("Arr list x = " + arrlistx);
        System.out.println("Arr list y = " + arrlisty);
        System.out.println("Color = " + colorpicker.getValue());
        System.out.println("Brush Size = " + bsize.getText());
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

}
