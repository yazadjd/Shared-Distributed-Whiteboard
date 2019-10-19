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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.sql.Array;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    private TextArea textdisplay;

    @FXML
    private TextArea chatmessage;

    //@FXML
    //private Button sendbutton;

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
    private File safile;
    public static Socket clientSocket;
    public static DataInputStream ipStream;
    public static DataOutputStream opStream;
    public String username;


    String incomingMsg = "";

    public void socketInitialize() throws IOException {
        clientSocket = new Socket("localhost", 2000);
        ipStream = new DataInputStream(clientSocket.getInputStream());
        opStream = new DataOutputStream(clientSocket.getOutputStream());
    }
    public void sendCanvas() throws IOException {
        opStream.writeUTF("Hello Canvas Sending");
    }

    public void sendMessage() throws IOException {
        opStream.writeUTF("Message sending");
    }

    public void threadInitialise(URL url, ResourceBundle resourceBundle){
        try {
            socketInitialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread t = new Thread(() -> handleCanvas(url, resourceBundle));
        t.start();
        Thread t2 = new Thread(() -> {
            try {
                handleChat();
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        });
        t2.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TextInputDialog username_dialog = new TextInputDialog();
        username_dialog.setTitle("USERNAME");
        username_dialog.setContentText("Please Enter a Suitable Username");
        Optional<String> user_name = username_dialog.showAndWait();
        if (user_name.isPresent()){
            username = user_name.get();
        }

        threadInitialise(url, resourceBundle);
    }

    private void handleCanvas(URL url, ResourceBundle resourceBundle) {
        brushTool = canvas.getGraphicsContext2D();
        //brushTool.setLineWidth(1);
        brushTool.setFill(Color.WHITE);
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
                brushTool.setFill(Paint.valueOf("#ffffff")); //#f2f2f2
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
            try {
                sendCanvas();
            } catch (IOException ex) {
                ex.printStackTrace();
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

    /*<Button fx:id="sendbutton" layoutX="272.0" layoutY="637.0" mnemonicParsing="false" onAction="#handleButtonAction" text="Send" />
    <Label fx:id="textdisplay" alignment="TOP_LEFT" layoutX="12.0" layoutY="60.0" prefHeight="525.0" prefWidth="310.0" wrapText="true" />
    @FXML
    public void handleButtonAction (ActionEvent event)
    {
        String messagec = chatmessage.getText();
        //operate(message_s, in, ou);
        chatmessage.setText("");
        String existingmess = textdisplay.getText();
        textdisplay.setText(existingmess + "\n\nClient 1: " + messagec);
    }*/
    private void handleChat() throws IOException, ParseException {
        JSONParser mess_parser = new JSONParser();

        while(true){
            incomingMsg = ipStream.readUTF();
            JSONObject server_jason = (JSONObject) mess_parser.parse(incomingMsg);
            String server_msg = (String) server_jason.get("Message_Content");
            String other_client_username = (String) server_jason.get("ClientUsername");
            if (server_msg != "") {
                String existing_mess = textdisplay.getText();
                textdisplay.setText(existing_mess + "\n\n" + other_client_username + ": " + server_msg);
                incomingMsg = "";
            }
        }
    }
    @FXML
    public void EnterPressed (KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER)
        {
            JSONObject message_parser = new JSONObject();
            message_parser.put("Request_Type", "Chat");
            message_parser.put("ClientUsername", username);
            String message_chat = chatmessage.getText();
            message_chat = message_chat.trim();
            chatmessage.setText("");
            String existing_mess = textdisplay.getText();
            textdisplay.setText(existing_mess + "\n\n" + username + ": " + message_chat);
            message_parser.put("Message_Content", message_chat);
            opStream.writeUTF(String.valueOf(message_parser));
        }
    }

    @FXML
    public void clicknew (ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("New Canvas");
        alert.setHeaderText("Are you sure you want to open a new canvas?");
        alert.setContentText("Unsaved changes will be lost.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            brushTool.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            safile = null;
        }
    }

    @FXML
    public void clickopen (ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Open Canvas");
        alert.setHeaderText("Are you sure you want to open an existing canvas?");
        alert.setContentText("Unsaved changes will be lost.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            FileChooser openfile = new FileChooser();
            openfile.setTitle("Open Canvas");
            File ofile = openfile.showOpenDialog(WhiteBoard.getInstance().getPrimaryStage());
            if (ofile != null)
            {
                try
                {
                    InputStream io = new FileInputStream(ofile);
                    Image img = new Image(io);
                    brushTool.drawImage(img, 0, 0);
                }
                catch (IOException e)
                {
                    //e.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Unable to open canvas: " + e).show();
                    //System.out.println("Unable to save image: " + e);
                }
            }
        }
    }

    @FXML
    public void clicksaveas (ActionEvent event) //Saves the current session with a user-defined name
    {
        FileChooser savefile = new FileChooser();
        savefile.setTitle("Save Canvas As...");
        safile = savefile.showSaveDialog(WhiteBoard.getInstance().getPrimaryStage());
        if (safile != null)
        {
            try
            {
                Image snapshot = canvas.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", safile);
                new Alert(Alert.AlertType.INFORMATION, "Your canvas has been saved successfully.").show();
            }
            catch (IOException e)
            {
                //e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Unable to save canvas: " + e).show();
                //System.out.println("Unable to save image: " + e);
            }
        }
    }

    @FXML
    public void clicksave (ActionEvent event) //Saves the current session by default as saved_session.png
    {
        if (safile == null)
        {
            FileChooser savefile = new FileChooser();
            savefile.setTitle("Save Canvas As...");
            safile = savefile.showSaveDialog(WhiteBoard.getInstance().getPrimaryStage());
            if (safile != null)
            {
                try
                {
                    Image snapshot = canvas.snapshot(null, null);
                    ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", safile);
                    new Alert(Alert.AlertType.INFORMATION, "Your canvas has been saved successfully.").show();
                }
                catch (IOException e)
                {
                    //e.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Unable to save canvas: " + e).show();
                    //System.out.println("Unable to save image: " + e);
                }
            }
        }
        else
        {
            try
            {
                Image snapshot = canvas.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", safile);
                new Alert(Alert.AlertType.INFORMATION, "Your canvas has been saved successfully.").show();
            }
            catch (IOException e)
            {
                //e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Unable to save canvas: " + e).show();
                //System.out.println("Unable to save image: " + e);
            }
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