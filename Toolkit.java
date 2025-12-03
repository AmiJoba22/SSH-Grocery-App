package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.UUID;

public class Toolkit {
    public static String generateShortID(String realName){
        realName = realName.trim().replace(" ", "_"); // Replace spaces with underscores

        int randomPartLength = 8; // Desired random part length
        String shortID = realName + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, randomPartLength);

        System.out.println("DEBUG: Generated User ID: " + shortID);
        return shortID;
    }

    public static Boolean askContinue(BufferedReader in, PrintWriter out) {
        try {
            while (true) { // Infinite loop until valid input or disconnection
                // Read input from the client
                String clientMessage = in.readLine();
                if (clientMessage == null) {
                    // Handle disconnection or input error
                    System.out.println("DEBUG: Client input was null (disconnection or error).");
                    out.println("Connection closed or input error.");
                    return false;
                }
                clientMessage = clientMessage.trim(); // Normalize the input by trimming whitespace
                if (!clientMessage.equalsIgnoreCase("y") && !clientMessage.equalsIgnoreCase("n")) {
                    // Invalid input, prompt user again
                    out.println("Invalid input. Please only enter 'y' or 'n'.");
                    // Continue the loop without printing "Connection closed or input error"
                    continue;
                }

                // Valid input: process it
                if (clientMessage.equalsIgnoreCase("y")) {
                    System.out.println("DEBUG: User wants to continue (y).");
                    return true;
                } else if (clientMessage.equalsIgnoreCase("n")) {
                    System.out.println("DEBUG: User wants to exit (n).");
                    return false;
                }
            }
        }catch (IOException e){
            System.out.println("DEBUG: IOException occurred.");
            return false;
        }
    }

    /*
     * Parse user input.
     * The input is split by whitespace.
     */

    public static String[] parseInput(String input) {
        return input.split("\\s+");
    }


    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
