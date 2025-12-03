package org.example;

import java.io.*;
import java.net.*;
import java.sql.*;

import org.apache.commons.codec.binary.Base64;

public class Server {
    private static final int DB_PORT = Credentials.PORT;
    private static int ONLINE_CLIENT = 0;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(DB_PORT)) {
            System.out.print("Server is running and waiting for a client...");
            // Server is running and waiting for a client
            // Can accept multiple clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ONLINE_CLIENT++;
                new ClientHandler(clientSocket).start();
                System.out.println("Online client: " + ONLINE_CLIENT);
            }

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}

class ClientHandler extends Thread {
    private final Socket clientSocket;
    Data.User currentUser = new Data.User(null, null, null, 0); // the current user information

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    // Handle client requests
    // This was named startPage in the original code
    public void run() {
        System.out.println("Client connected! ");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Handle client requests
            boolean restart = true;
            while (restart) {
                out.println("Welcome to SSH Cloud Grocery.");
                out.println("Enter '1' to register, '2' to login, '3' to exit.");

                String clientMessage = in.readLine();
                System.out.println("Received from client: " + clientMessage);
                switch (clientMessage) {
                    case "1": // Register
                        while (true) {
                            if (register(in, out)) {
                                out.println("Register successful! Back to main page.");
                                break;
                            } else {
                                out.println("Register failed. Do you want to try again? y/n");
                                if (!Toolkit.askContinue(in, out)) {
                                    break;
                                }
                            }
                        }
                        break;
                    case "2": // Login
                        while (true) {
                            if (login(in, out)) {
                                // login successful
                                out.println("Login successful! Continue to menu? y/n");
                                if (Toolkit.askContinue(in, out)) {
                                    // continue to menu
                                    Menu.displayMenu(currentUser, in, out);
                                }
                                // back to main page if not continue
                                break;
                            } else {
                                // login failed
                                out.println("Login failed. Make sure you are entering Short ID. Do you want to try again? y/n");
                                if (!Toolkit.askContinue(in, out)) {
                                    break;
                                }
                                // not continue, back to main page
                            }
                        }
                        break;
                    case "3":
                        out.println("Goodbye!");
                        // ask the client to exit
                        // close connection
                        clientSocket.close();
                        break;
                    default:
                        out.println("Unknown command: Enter '1' to register, '2' to login, '3' to exit.");
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Boolean register(BufferedReader in, PrintWriter out) throws IOException {
        try {
            out.println("Enter your name to register: ");
            String username = in.readLine();
            out.println("Set your password: ");
            String password = in.readLine();
            out.println("Where do you live? Select from the following address. Enter the house ID: ");
            // fetch house info from database
            out.println(Service.showHouseInfo());
            // make sure house_id is correct from database
            // user did not input an integer
            String house_id_String = in.readLine();
            if (!Toolkit.isInteger(house_id_String)) {
                out.println("Invalid input. Please enter a number.");
                return false;
            }

            int house_id = Integer.parseInt(house_id_String);

            // encrypt password
            password = new String(Base64.encodeBase64(password.getBytes()));

            // generate a shortID for the user
            String shortID = Toolkit.generateShortID(username);

            try (Connection connection = new Service().getConnection();

                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE short_id = ?")) {
                statement.setString(1, shortID);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    out.println("User already exists!");
                } else {
                    try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO users (user_name, user_password, house_id, short_id) VALUES (?, ?, ?, ?)")) {
                        insertStatement.setString(1, username);
                        insertStatement.setString(2, password);
                        insertStatement.setInt(3, house_id);
                        insertStatement.setString(4, shortID);
                        insertStatement.executeUpdate();
                        out.println("User registered! Your Short ID is: " + shortID + ".\n" +
                                "Please keep it safe. You will need to use it to login.");
                        return true;
                    }
                }

            } catch (SQLException e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }

        return false;
    }

    private Boolean login(BufferedReader in, PrintWriter out) throws IOException {
        try {
            out.println("Enter your Short ID: ");
            String short_id = in.readLine();
            out.println("Enter your password: ");
            String password = in.readLine();

            // encrypt password
            password = new String(Base64.encodeBase64(password.getBytes()));

            try (Connection connection = new Service().getConnection();
                 // check if password match
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE short_id = ? AND user_password = ?")) {
                statement.setString(1, short_id);
                statement.setString(2, password);
                ResultSet resultSet = statement.executeQuery();

                // check if user exists
                if (resultSet.next()) {
                    // login successful, go to main page with menu
                    System.out.println("User " + short_id + " logged in successful.");
                    // statement to get user_id
                    PreparedStatement statement1 = connection.prepareStatement("SELECT * FROM users WHERE short_id = ?");
                    statement1.setString(1, short_id);
                    ResultSet resultSet1 = statement1.executeQuery();
                    // set currentUserUUID
                    if (resultSet1.next()) {
                        // Set the current user information
                        currentUser.setShort_id(short_id);
                        currentUser.setUser_name(resultSet1.getString("user_name"));
                        currentUser.setHouse_id(resultSet1.getInt("house_id"));
                        currentUser.setUser_id(resultSet1.getObject("user_id").toString()); // UUID
                        System.out.println("DEBUG: Current user information: " + currentUser.toString());
                    }

                    return true;
                } else
                    // user not exists
                    System.out.println("User " + short_id + " not found.");

            } catch (SQLException e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }

        return false;
    }

}

