package org.example;

import java.io.*;
import java.net.*;

public class Client {
    private static final String HOST = Credentials.HOST;
    private static final int PORT = Credentials.PORT;

    public static void main(String[] args) {
        // waiting for the server to accept the connection
        System.out.print("Connecting to the server...");

        // set timeout to 5 seconds
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), 5000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Connected.");
            String userInput;

            // Thread to read server responses
            Thread serverResponseThread = new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = in.readLine()) != null) {
                        System.out.println("Server: " + serverResponse);
                        if ("Goodbye!".equalsIgnoreCase(serverResponse) || "Server: Goodbye!".equalsIgnoreCase(serverResponse)) {
                            System.exit(0);
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("An error occurred: " + e.getMessage());
                }
            });

            serverResponseThread.start();
            // Main thread to read user input
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
            }


        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}