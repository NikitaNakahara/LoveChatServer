import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Server {
    boolean stopServer = false;

    public void start() throws IOException {
        try (ServerSocket server = new ServerSocket(8080)) {
            System.out.println("Start server");

            Scanner scanner = new Scanner(System.in);
            new Thread(() -> {
                while (true) {
                    switch (scanner.nextLine()) {
                        case "stop":
                            if (!stopServer) {
                                stopServer = true;
                            } else {
                                System.out.println("server already stopped\n");
                            }
                            break;

                        case "start":
                            if (stopServer) {
                                stopServer = false;
                                new Thread(() -> startMainLoop(server));
                            } else {
                                System.out.println("server already started\n");
                            }
                            break;

                        default:
                            System.out.println("unexpected command\n");
                            break;
                    }
                }
            }).start();

            startMainLoop(server);
        }
    }

    private void startMainLoop(ServerSocket server) {
        while (!stopServer) {
            try {
                newClient(server.accept());
            } catch (IOException e) {
                e.printStackTrace();
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
