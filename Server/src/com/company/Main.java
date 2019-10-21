package com.company;

import javax.net.ServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main
{
    public static int counter = 0;
    public static ArrayList<Socket> clients_socket_dir;
    public static ArrayList<String> clients_uname_dir = new ArrayList<String>();

    public static void main(String[] args) throws IOException
    {

        ServerSocketFactory factory = ServerSocketFactory.getDefault();

        clients_socket_dir = new ArrayList<Socket>();

        try(ServerSocket server = factory.createServerSocket(2000)) {

            System.out.println("Waiting for client connection-");

            // Wait for connections.
            while(true)
            {
                Socket client = server.accept();
                counter++;
                System.out.println("Client "+counter+": Applying for connection!");

                // Start a new thread for a connection
                Thread t = new Thread(() -> serveClient(client));
                t.start();

            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void serveClient(Socket socket)
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

            if (clients_uname_dir.size() > 1)
            {
                JSONObject notify_manager = new JSONObject();
                notify_manager.put("Request_Type", "Notify");
                notify_manager.put("ClientUsername", first_message);
                (new DataOutputStream(clients_socket_dir.get(0).getOutputStream())).writeUTF(String.valueOf(notify_manager));
            }

            System.out.println(clients_uname_dir);
            JSONObject message_parser = new JSONObject();
            message_parser.put("Request_Type", "Member");
            message_parser.put("ClientUsername", "random");
            broadcastUserListToOtherClients(message_parser, clients_uname_dir, clientSocket);
            /*try
            {
                //ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                for (int i = 0; i < clients_socket_dir.size(); i++)
                {

                    if(clients_socket_dir.get(i) == clientSocket)
                    {
                        continue;
                    }
                    else{
                        (new DataOutputStream(clients_socket_dir.get(i).getOutputStream())).writeUTF(String.valueOf(message_parser));
                        (new ObjectOutputStream(clients_socket_dir.get(i).getOutputStream())).writeObject(clients_uname_dir);
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }*/
            //JSONParser socket_parser = new JSONParser();
            //JSONObject socket_dict = (JSONObject) socket_parser.parse(first_message);

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
                    String canvas_length = (String) client_message.get("CanvasLength");
                    length = Integer.parseInt(canvas_length);
                    if (length > 0)
                    {
                        byte[] message = new byte[length];
                        input.readFully(message, 0, message.length);
                        broadcastCanvasToOtherClients(client_message, message, clientSocket);
                    }
                }
                else if (request_type.matches("ManagerExit"))
                {
                    broadcastMessageToOtherClients(client_message, clientSocket);
                    clients_socket_dir.clear();
                    clients_uname_dir.clear();
                }
                else if (request_type.matches("ClientExit"))
                {
                    client_message.put("Request_Type", "Member");
                    clients_uname_dir.remove(user);
                    broadcastUserListToOtherClients(client_message, clients_uname_dir, clientSocket);
                    clients_socket_dir.remove(clientSocket);
                    //return 0;
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("Test");
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