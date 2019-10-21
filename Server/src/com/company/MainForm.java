package com.company;

import javax.net.ServerSocketFactory;
import javax.swing.*;
import java.awt.event.*;
import javax.net.ServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MainForm {
    private JPanel contentPane;
    private JTextArea textArea1;
    private JLabel appTitle;
    private JButton buttonOK;
    private JButton buttonCancel;
    public static int counter = 0;
    public static ArrayList<Socket> clients_socket_dir;
    public static ArrayList<String> clients_uname_dir = new ArrayList<String>();
    public static HashMap<String, Socket> client_dir;
    public static byte[] message;
    public static String canvas_length = "0";
    public static String str;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Server");
        MainForm server_app = new MainForm();
        frame.setContentPane(server_app.contentPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        ServerSocketFactory factory = ServerSocketFactory.getDefault();

        clients_socket_dir = new ArrayList<Socket>();
        client_dir = new HashMap<>();

        try(ServerSocket server = factory.createServerSocket(2000)) {

            server_app.textArea1.setText("Waiting for client connection-");

            // Wait for connections.
            while(true)
            {
                Socket client = server.accept();
                counter++;
                str = server_app.textArea1.getText();
                server_app.textArea1.setText(str + "\nClient "+counter+": Applying for connection!");

                // Start a new thread for a connection
                Thread t = new Thread(() -> serveClient(client, server_app));
                t.start();

            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private static void serveClient(Socket socket, MainForm server_app)
    {
        try (Socket clientSocket = socket)
        {
            JSONParser mess_parser = new JSONParser();
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            clients_socket_dir.add(clientSocket);
            //clients_uname_dir.add(first_message);
            try
            {
                ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                objectOutput.writeObject(clients_uname_dir);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            String first_message = input.readUTF();
            clients_uname_dir.add(first_message);
            client_dir.put(first_message, socket);

            if (clients_uname_dir.size() > 1)
            {
                JSONObject notify_manager = new JSONObject();
                notify_manager.put("Request_Type", "Notify");
                notify_manager.put("ClientUsername", first_message);
                (new DataOutputStream(clients_socket_dir.get(0).getOutputStream())).writeUTF(String.valueOf(notify_manager));
            }
            str = server_app.textArea1.getText();
            server_app.textArea1.setText(str + "\n" + clients_uname_dir);
            JSONObject message_parser = new JSONObject();
            message_parser.put("Request_Type", "Member");
            message_parser.put("ClientUsername", "random");
            broadcastUserListToOtherClients(message_parser, clients_uname_dir, clientSocket);

            while (true)
            {
                String new_message = input.readUTF();
                JSONObject client_message = (JSONObject) mess_parser.parse(new_message);
                String request_type = (String) client_message.get("Request_Type");
                String user = (String) client_message.get("ClientUsername");
                int length;
                if (request_type.matches("Chat"))
                {
                    String message_content = (String) client_message.get("Message_Content");
                    if (!message_content.equals(""))
                    {
                        broadcastMessageToOtherClients(client_message, clientSocket);
                    }
                }
                else if (request_type.matches("Canvas"))
                {
                    canvas_length = (String) client_message.get("CanvasLength");
                    length = Integer.parseInt(canvas_length);
                    if (length > 0)
                    {
                        message = new byte[length];
                        input.readFully(message, 0, message.length);
                        broadcastCanvasToOtherClients(client_message, message, clientSocket);
                    }
                }
                else if (request_type.matches("ManagerExit"))
                {
                    broadcastMessageToOtherClients(client_message, clientSocket);
                    clients_socket_dir.clear();
                    clients_uname_dir.clear();
                    canvas_length = "0";
                    message = null;
                }
                else if (request_type.matches("ClientExit"))
                {
                    client_message.put("Request_Type", "Member");
                    Socket port_of_denied_user = client_dir.get(user);
                    clients_uname_dir.remove(user);
                    broadcastUserListToOtherClients(client_message, clients_uname_dir, port_of_denied_user);
                    clients_socket_dir.remove(port_of_denied_user);
                    port_of_denied_user.close();
                    //return 0;
                }
                else if (request_type.matches("DenyAccess"))
                {
                    Socket port_of_denied_user = client_dir.get(user);
                    clients_uname_dir.remove(user);
                    (new DataOutputStream(port_of_denied_user.getOutputStream())).writeUTF(String.valueOf(client_message));
                    clients_socket_dir.remove(port_of_denied_user);
                    client_dir.remove(user);
                }
                else if(request_type.matches("RemoveUser")) {
                    client_message.put("Request_Type", "Member");
                    Socket port_of_denied_user = client_dir.get(user);
                    clients_uname_dir.remove(user);
                    broadcastUserListToOtherClients(client_message, clients_uname_dir, port_of_denied_user);
                    client_message.put("Request_Type", "RemoveUser");
                    (new DataOutputStream(port_of_denied_user.getOutputStream())).writeUTF(String.valueOf(client_message));
                    clients_socket_dir.remove(port_of_denied_user);
                }
                else if (request_type.matches("Get Canvas")) {
                    JSONObject cl_message = new JSONObject();
                    cl_message.put("Request_Type", "Get Canvas");
                    cl_message.put("ClientUsername", clients_uname_dir.get(clients_uname_dir.size() - 1));
                    cl_message.put("CanvasLength", canvas_length);
                    (new DataOutputStream(clients_socket_dir.get((clients_socket_dir).size() - 1).getOutputStream())).writeUTF(String.valueOf(cl_message));
                    if (message != null) {
                        (new DataOutputStream(clients_socket_dir.get((clients_socket_dir).size() - 1).getOutputStream())).write(message);
                    }
                }
            }
        }
        catch (IOException e)
        {
            clients_socket_dir.remove(socket);
            //clients_uname_dir.remove();
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }
    public static void broadcastMessageToOtherClients(JSONObject client_message, Socket clientSocket) throws IOException {


        for (int i = 0; i < clients_socket_dir.size(); i++) {
            if(clients_socket_dir.get(i) == clientSocket) {
                continue;
            }
            else{
                (new DataOutputStream(clients_socket_dir.get(i).getOutputStream())).writeUTF(String.valueOf(client_message));
            }
        }
    }

    public static void broadcastCanvasToOtherClients(JSONObject client_message, byte[] message, Socket clientSocket) throws IOException {
        for (int i = 0; i < clients_socket_dir.size(); i++) {
            if(clients_socket_dir.get(i) == clientSocket) {
                continue;
            }
            else{
                //String messages = "servercanvas:" + Integer.toString(message.length);
                (new DataOutputStream(clients_socket_dir.get(i).getOutputStream())).writeUTF(String.valueOf(client_message));
                (new DataOutputStream(clients_socket_dir.get(i).getOutputStream())).write(message);
            }
        }
    }

    public static void broadcastUserListToOtherClients(JSONObject client_message, ArrayList<String> user_name, Socket clientSocket) throws IOException {
        try
        {
            //ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
            for (int i = 0; i < clients_socket_dir.size(); i++)
            {

                if(clients_socket_dir.get(i) == clientSocket)
                {
                    continue;
                }
                else{
                    (new DataOutputStream(clients_socket_dir.get(i).getOutputStream())).writeUTF(String.valueOf(client_message));
                    (new ObjectOutputStream(clients_socket_dir.get(i).getOutputStream())).writeObject(user_name);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
