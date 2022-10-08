import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    boolean stopServer = false;

    Map<String, DataOutputStream> usersMap = new HashMap<>();

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
                    System.out.println(message);
                    JSONObject json = new JSONObject(message);
                    switch (json.getString("type")) {
                        case "msg": {
                            Map<String, String> map = new HashMap<>();
                            map.put("type", "msg");
                            map.put("text", json.getString("text"));
                            DataOutputStream dos = findUserInOnline(json.getString("id"));
                            if (dos != null) {
                                dos.writeUTF(new JSONObject(map).toString());
                            } else {
                                System.out.println("user not found");
                            }
                            break;
                        }

                        case "sys": {
                            switch (json.getString("text")) {
                                case "get_id": {
                                    String id = genUniqueId();
                                    Map<String, String> map = new HashMap<>();
                                    map.put("type", "sys");
                                    map.put("text", "user_id");
                                    map.put("id", id);
                                    output.writeUTF(new JSONObject(map).toString());
                                    break;
                                }

                                case "online": {
                                    String id = json.getString("id");
                                    addUserInOnline(output, id);
                                    break;
                                }
                            }

                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Client is disconnected");
                    break;
                }
            }
        }).start();
    }



    private String genUniqueId() {
        StringBuilder id = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            int randomInt = random.nextInt(9);
            id.append(randomInt);
        }

        return id.toString();
    }

    private void addUserInOnline(DataOutputStream output, String id) {
        usersMap.put(id, output);
    }

    private DataOutputStream findUserInOnline(String id) {
        return usersMap.get(id);
    }
}
