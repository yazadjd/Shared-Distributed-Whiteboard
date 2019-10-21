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
import javafx.scene.control.Menu;
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
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.sql.Array;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javafx.stage.Stage;
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

    @FXML
    private Menu filemenu;

    @FXML
    private Menu privilegemenu;

    @FXML
    private Button showmembers;

    @FXML
    private Label ismanager;

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
    public static ArrayList<String> clients_uname_list;
    public static int flag = 0;
    public static int manager = 0;

    String incomingMsg = "";

    public void socketInitialize() throws IOException
    {
        clientSocket = new Socket("localhost", 2000);
        ipStream = new DataInputStream(clientSocket.getInputStream());
        opStream = new DataOutputStream(clientSocket.getOutputStream());

        //message_parser.put("ClientUsername", username);
        try
        {
            ObjectInputStream objectInput = new ObjectInputStream(clientSocket.getInputStream());
            try
            {
                Object object = objectInput.readObject();
                clients_uname_list =  (ArrayList<String>) object;
                //new Alert(Alert.AlertType.INFORMATION, "Received list!").show();
                //System.out.println("Received list!");
                //System.out.println(clients_uname_list.get(1));
            }
            catch (ClassNotFoundException e)
            {
                //e.printStackTrace();
                System.out.println(e);
            }
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            System.out.println(e);
        }
        if (clients_uname_list.isEmpty())
        {
            System.out.println("You are the manager!");
            manager = 1;
            //new Alert(Alert.AlertType.INFORMATION, "You are the manager!").show();
        }
        else
        {
            while (clients_uname_list.contains(username))
            {
                //new Alert(Alert.AlertType.INFORMATION, "Use a different username!").show();
                TextInputDialog username_dialog = new TextInputDialog();
                username_dialog.setTitle("USERNAME");
                username_dialog.setContentText("Please enter another username");
                Optional<String> user_name = username_dialog.showAndWait();
                if (user_name.isPresent())
                {
                    username = user_name.get();
                    if (!clients_uname_list.contains(username))
                    {
                        break;
                    }
                }
            }
        }
        clients_uname_list.add(username);
        opStream.writeUTF(username);
    }
    public void sendCanvas() throws IOException {
        JSONObject message_parser = new JSONObject();
        message_parser.put("Request_Type", "Canvas");
        message_parser.put("ClientUsername", username);
        //opStream.writeUTF("Hello Canvas Sending");
        try
        {
            Image snapshot = canvas.snapshot(null, null);
            BufferedImage bImage = SwingFXUtils.fromFXImage(snapshot, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bImage, "png", baos );
            byte [] data = baos.toByteArray();
            message_parser.put("CanvasLength", String.valueOf(data.length));
            opStream.writeUTF(String.valueOf(message_parser));
            opStream.write(data);
            //new Alert(Alert.AlertType.INFORMATION, "Your canvas has been saved successfully.").show();
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Unable to send canvas: " + e).show();
            //System.out.println("Unable to save image: " + e);
        }
    }

    public void sendMessage(String m) throws IOException {
        opStream.writeUTF(m);
        //opStream.writeUTF("Message sending");
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
            } catch (IOException | ParseException | InterruptedException e) {
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
    public void brushfill(double x, double  y, double size) {
        brushTool.setFill(colorpicker.getValue());
        size = Double.parseDouble(bsize.getText());
        brushTool.fillRoundRect(x, y, size, size, size, size);
    }
    public void erase(double x, double y, double size) {
        brushTool.setFill(Paint.valueOf("#ffffff"));
        size = Double.parseDouble(bsize.getText());
        brushTool.fillRoundRect(x, y, size, size, size, size);
    }
    public void rectangle(double x, double y, double width, double height) {
        brushTool.setFill(colorpicker.getValue());
        brushTool.fillRect(x, y, width, height);
    }
    public void Ellipse(double x, double y, double axisx, double axisy) {
        brushTool.setFill(colorpicker.getValue());
        brushTool.fillOval(x, y, axisx, axisy);
    }
    public void drawLine(double init_x, double init_y, double x, double y){
        brushTool.setStroke(colorpicker.getValue());
        brushTool.setLineWidth(size);
        brushTool.strokeLine(init_x, init_y, x, y);
    }
    public void insertText(String text, double x, double  y, double siz){
        brushTool.setFill(colorpicker.getValue());
        brushTool.setStroke(colorpicker.getValue());
        brushTool.setFont(new Font(siz));
        brushTool.fillText(text, x, y);
    }
    private void handleCanvas(URL url, ResourceBundle resourceBundle) {
        brushTool = canvas.getGraphicsContext2D();
        //brushTool.setLineWidth(1);
        brushTool.setFill(Color.WHITE);
        toolSelected = "brush"; // default tool selected
        bsize.setText("10"); //Default size
        colorpicker.setValue(Color.BLACK); //Default color
        //Use the below options for non-managers
        if (manager == 0)
        {
            newoption.setDisable(true);
            openoption.setDisable(true);
            saveoption.setDisable(true);
            saveasoption.setDisable(true);
            privilegemenu.setDisable(true);
            ismanager.setText("Username: " + username);
        }
        else
        {
            ismanager.setText("Manager: " + username);
        }

        /*if (manager == 0)
        {

        }*/

        canvas.setOnMouseClicked(e -> {
            x = e.getX() - size / 2;
            y = e.getY() - size / 2;

            if (toolSelected.matches("text") && !bsize.getText().isEmpty()) {
                TextInputDialog text_input = new TextInputDialog();
                text_input.setTitle("TEXT BOX");
                text_input.setContentText("Please Enter Text to Display");
                Optional<String> text_display = text_input.showAndWait();
                String text_value = String.valueOf(text_display.get());
                double size_text = Double.parseDouble(bsize.getText());
                insertText(text_value, x,  y, size_text);
            }
            try {
                sendCanvas();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

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
            if(toolSelected.matches("eraser") && !bsize.getText().isEmpty()){
                erase(x, y, size);
                arrlistx.add(x);
                arrlisty.add(y);
            }

            if(toolSelected.matches("brush") && !bsize.getText().isEmpty()){
                brushfill(x, y, size);
                arrlistx.add(x);
                arrlisty.add(y);
            }
            if(toolSelected.matches("rectangle")){

                if((x - init_x ) < 0) {
                    if ((y - init_y) < 0 ){
                        rectangle(x, y, init_x-x, init_y - y);
                    }
                    else {
                        rectangle(x, init_y, init_x-x, y - init_y);
                    }
                }
                else if ((x - init_x) > 0){
                    if ((y - init_y) > 0) {
                        rectangle(init_x, init_y,x - init_x  ,y - init_y);
                    }
                    else {
                        rectangle(init_x, y, x - init_x, init_y - y);
                    }
                }
            }
        });

        canvas.setOnMouseReleased( e -> {

            if (toolSelected.matches("circle")) {
                if((x - init_x ) < 0) {
                    if ((y - init_y) < 0 ){
                        Ellipse(x, y, (init_x-x + init_y-y)/2, (init_x-x + init_y-y)/2);
                        arrlistx.add(x);
                        arrlistx.add((init_x-x + init_y-y)/2);
                        arrlisty.add(y);
                        arrlisty.add((init_x-x + init_y-y)/2);
                    }
                    else {
                        Ellipse(x, y, (init_x-x + y - init_y)/2, (init_x-x + y - init_y)/2);
                        arrlistx.add(x);
                        arrlistx.add((init_x-x + y - init_y)/2);
                        arrlisty.add(y);
                        arrlisty.add((init_x-x + y - init_y)/2);
                    }
                }
                else if ((x - init_x) > 0) {
                    if ((y - init_y) > 0) {
                        Ellipse(init_x, init_y, (x - init_x + y - init_y) / 2, (x - init_x + y - init_y) / 2);
                        arrlistx.add(init_x);
                        arrlistx.add((x - init_x + y - init_y) / 2);
                        arrlisty.add(init_y);
                        arrlisty.add((x - init_x + y - init_y) / 2);
                    }
                    else {
                        Ellipse(init_x, init_y, (x - init_x + init_y - y) / 2, (x - init_x + init_y - y) / 2);
                        arrlistx.add(init_x);
                        arrlistx.add((x - init_x + init_y - y) / 2);
                        arrlisty.add(init_y);
                        arrlisty.add((x - init_x + init_y - y) / 2);
                    }
                }
            }
            if(toolSelected.matches("line") && !bsize.getText().isEmpty()){

                drawLine(init_x, init_y, x, y);
                arrlistx.add(init_x);
                arrlistx.add(x);
                arrlisty.add(init_y);
                arrlisty.add(y);
            }
            if (toolSelected.matches("rectangle"))
            {
                arrlistx.add(init_x);
                arrlisty.add(init_y);
                arrlistx.add(x - init_x);
                arrlisty.add(y - init_y);
            }
            if (toolSelected.matches("oval")) {
                if((x - init_x ) < 0) {
                    if ((y - init_y) < 0 ){
                        Ellipse(x, y, init_x - x, init_y - y);
                        arrlistx.add(x);
                        arrlistx.add(init_x-x);
                        arrlisty.add(y);
                        arrlisty.add(init_y-y);
                    }
                    else {
                        Ellipse(x, y, init_x - x , y - init_y);
                        arrlistx.add(x);
                        arrlistx.add(init_x-x);
                        arrlisty.add(y);
                        arrlisty.add(y - init_y);
                    }
                }
                else if ((x - init_x) > 0) {
                    if ((y - init_y) > 0) {
                        Ellipse(init_x, init_y, x - init_x, y - init_y);
                        arrlistx.add(init_x);
                        arrlistx.add(x - init_x);
                        arrlisty.add(init_y);
                        arrlisty.add(y - init_y);
                    }
                    else {
                        Ellipse(init_x, init_y, x - init_x , init_y - y);
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
    public void textSelected(ActionEvent e)
    {
        toolSelected = "text";
    }

    @FXML
    public void showmemberlist (ActionEvent event)
    {
        String memlist = "";
        //new Alert(Alert.AlertType.INFORMATION, username).show();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Members List");
        alert.setHeaderText("List of participants in the current session");
        for (int i = 0; i < clients_uname_list.size(); i++)
        {
            memlist = memlist + clients_uname_list.get(i) + "\n";
        }
        alert.setContentText(memlist);
        alert.show();
    }

    private void handleChat() throws IOException, ParseException, InterruptedException {
        JSONParser mess_parser = new JSONParser();

        while (true) {
            String new_message = ipStream.readUTF();
            JSONObject client_message = (JSONObject) mess_parser.parse(new_message);
            String request_type = (String) client_message.get("Request_Type");
            String user = (String) client_message.get("ClientUsername");
            if (request_type.matches("Chat")) {
                String message_content = (String) client_message.get("Message_Content");
                if (!message_content.equals("")) {
                    String existing_mess = textdisplay.getText();
                    textdisplay.setText(existing_mess + "\n\n"+ user + ": " + message_content);
                    incomingMsg = "";
                }
            }
            else if (request_type.matches("Canvas"))
            {
                String canvas_length = (String) client_message.get("CanvasLength");
                Integer length = Integer.parseInt(canvas_length);
                if (length > 0)
                {
                    byte[] message = new byte[length];
                    ipStream.readFully(message, 0, message.length);
                    ByteArrayInputStream bais = new ByteArrayInputStream(message);
                    BufferedImage bImage2 = ImageIO.read(bais);
                    Image img = SwingFXUtils.toFXImage(bImage2, null);
                    while (canvas.isPressed()) {
                        flag = 1;
                    }
                    brushTool.drawImage(img, 0, 0);
                    if (flag == 1) {
                        if (toolSelected.matches("brush")) {
                            for (int point = 0; point < arrlistx.size(); point++) {
                                brushTool.fillRoundRect(arrlistx.get(point), arrlisty.get(point), size, size, size, size);
                            }
                        } else if (toolSelected.matches("eraser")) {
                            for (int point = 0; point < arrlistx.size(); point++) {
                                erase(arrlistx.get(point), arrlisty.get(point), size);
                            }
                        } else if (toolSelected.matches("rectangle")) {
                            rectangle(arrlistx.get(0), arrlisty.get(0), arrlistx.get(1), arrlisty.get(1));
                        } else if (toolSelected.matches("circle")) {
                            Ellipse(arrlistx.get(0), arrlisty.get(0), arrlistx.get(1), arrlisty.get(1));
                        } else if (toolSelected.matches("oval")) {
                            Ellipse(arrlistx.get(0), arrlisty.get(0), arrlistx.get(1), arrlisty.get(1));
                        } else if (toolSelected.matches("line")) {
                            drawLine(arrlistx.get(0), arrlisty.get(0), arrlistx.get(1), arrlisty.get(1));
                        }
                        flag = 0;
                        sendCanvas();
                    }
                }
            }
            else if (request_type.matches("Member"))
            {
                try
                {
                    ObjectInputStream objectInput = new ObjectInputStream(clientSocket.getInputStream());
                    try
                    {
                        Object object = objectInput.readObject();
                        clients_uname_list =  (ArrayList<String>) object;
                    }
                    catch (ClassNotFoundException e)
                    {
                        //e.printStackTrace();
                        System.out.println(e);
                    }
                }
                catch (IOException e)
                {
                    //e.printStackTrace();
                    System.out.println(e);
                }
            }
            else if (request_type.matches("Exit"))
            {
                //canvas.setAccessibleText("The manager has terminated the session");
                brushTool.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                brushTool.strokeText("The manager has terminated the session", canvas.getWidth()/2, canvas.getHeight()/2);
                //new Alert(Alert.AlertType.INFORMATION, "The manager has terminated the session").show();
                //System.out.println("The manager has terminated the session");
                TimeUnit.SECONDS.sleep(5);
                System.exit(0);
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
            sendMessage(String.valueOf(message_parser));
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
    public void clickclose (ActionEvent event) throws IOException //Closes the current session
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Close Whiteboard");
        alert.setHeaderText("Are you sure you want to exit the Whiteboard?");
        alert.setContentText("Unsaved changes will be lost.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            if (manager == 1)
            {
                JSONObject message_parser = new JSONObject();
                message_parser.put("Request_Type", "Exit");
                message_parser.put("ClientUsername", "random");
                opStream.writeUTF(String.valueOf(message_parser));
            }
            System.exit(0);
        }
    }
}