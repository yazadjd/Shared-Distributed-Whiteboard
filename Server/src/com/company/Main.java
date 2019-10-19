package com.company;

import javax.net.ServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import org.json.*;

public class Main {
    public static int counter = 0;
    public static ArrayList<Socket> clients_socket_dir;

    public static void main(String[] args) throws IOException {

        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        clients_socket_dir = new ArrayList<Socket>();

        try(ServerSocket server = factory.createServerSocket(2000))
        {
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

    private static void serveClient(Socket socket) {
        try (Socket clientSocket = socket) {


            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            clients_socket_dir.add(clientSocket);

            while (true) {
                String new_message = input.readUTF();
                if (message_content != "") {
                    String client_username = (String) message_from_client.get("ClientUsername");
                    broadcastMessageToOtherClients(message_content, clientSocket, client_username);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessageToOtherClients(String message, Socket clientSocket, String client_username) throws IOException {
        for (int i = 0; i < clients_socket_dir.size(); i++) {
            if(clients_socket_dir.get(i) == clientSocket) {
                continue;
            }
            else{
                (new DataOutputStream(clients_socket_dir.get(i).getOutputStream())).writeUTF(message);
            }
        }
    }
}
