import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12346;
    private static Thread listener; // Declare listener thread

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            Scanner scanner = new Scanner(System.in);
            listener = new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        // Only print messages from the server
                        if (message.startsWith("Server: ")) {
                            System.out.println(message);
                        }
                    }
                } catch (IOException e) {
                    // Handle the exception if the stream is closed
                    if (!socket.isClosed()) {
                        e.printStackTrace();
                    }
                }
            });
            listener.start();

            String userInput;
            while (true) {
                userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    out.println("Client has left the chat.");
                    break;
                }
                out.println(userInput); // Send user input to the server
            }
            listener.interrupt(); // Interrupt the listener thread
            socket.close(); // Close the socket
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 