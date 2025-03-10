import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12346;
    private static Set<ClientHandler> clientHandlers = new HashSet<>(); // Store client handlers
    private static int clientCount = 0; // Counter for clients

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Start a thread to read server input
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String serverInput = scanner.nextLine();
                    // Check if the input is a command to send a message to a specific client
                    if (serverInput.startsWith("send ")) {
                        String[] parts = serverInput.split(" ", 3);
                        if (parts.length == 3) {
                            int clientId = Integer.parseInt(parts[1]);
                            String message = parts[2];
                            sendToClient(clientId, "Server: " + message);
                        } else {
                            System.out.println("Usage: send <client_id> <message>");
                        }
                    } else {
                        broadcast("Server: " + serverInput); // Broadcast server messages
                    }
                }
            }).start();

            while (true) {
                new ClientHandler(serverSocket.accept(), ++clientCount).start(); // Increment and pass client count
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private int clientNumber; // Unique client number

        public ClientHandler(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber; // Assign client number
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientHandlers) {
                    clientHandlers.add(this); // Add this client handler to the set
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Client " + clientNumber + ": " + message);
                    broadcast("Client " + clientNumber + ": " + message); // Broadcast with client number
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientHandlers) {
                    clientHandlers.remove(this); // Remove this client handler from the set
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message); // Send a message to this client
        }
    }

    private static void broadcast(String message) {
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.sendMessage(message); // Use the sendMessage method
            }
        }
    }

    private static void sendToClient(int clientId, String message) {
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                if (handler.clientNumber == clientId) { // Check if this is the correct client
                    handler.sendMessage(message); // Send message to the specific client
                    return; // Exit after sending the message
                }
            }
            System.out.println("Client " + clientId + " not found.");
        }
    }
} 