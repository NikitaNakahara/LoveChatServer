import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Server {
    public void start() throws IOException {

        try (ServerSocket server = new ServerSocket(8080)) {
            System.out.println("Start server");
            //noinspection InfiniteLoopStatement
            while (true) {
                newClient(server.accept());
            }
        }
    }

    private void newClient(Socket client) throws IOException {
        System.out.println("Client connected");
        DataInputStream input = new DataInputStream(client.getInputStream());
        DataOutputStream output = new DataOutputStream(client.getOutputStream());

        new Thread(() -> {
            System.out.println("Thread created");
            while (!client.isClosed()) {
                try {
                    String message = input.readUTF();
                    output.writeUTF(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
