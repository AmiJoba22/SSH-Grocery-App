package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Menu {
    // List of Products
    private static List<Data.Product> products = new ArrayList<>();
    private static int currentPage = 1;
    private static final int PRODUCTS_PER_PAGE = 10;

    //Possible Load sample product

    //Display Menu
    public static void displayMenu(Data.User currentUser, BufferedReader in, PrintWriter out) throws SQLException {
        // The system will create an order when user goes to the menu
        // if user do not have active order, create one
        if (Order.getTargetOrderID(currentUser) == -1) {
            System.out.println("displayMenu: User does not have an active order. Creating one now.");
            Order.createOrder(currentUser);
        }else{
            System.out.println("displayMenu: User already have a order.");
        }

        // when showing the shopping cart, we are actually showing
        // 1. if user in a group, then show their own order
        // 2. if user not in a group, then show all groups in the house

        boolean restart = true;
        while (restart){
            out.println("===== SSH Cloud Grocery Menu =====");
            out.println("1. View Products");
            out.println("2. My Shopping Cart and Group");
            out.println("3. Back to main page");
            out.println("4. Exit");

            out.println("Choose an option: ");
            try {
                String option = in.readLine();

                switch (option) {
                    case "1":
                        //View products
                        // TODO see all products available in the database
                        //showProducts(currentPage, in, out); //TODO put the method into Order?
                        showProducts(currentUser, currentPage, in, out);
                        // give user options to add products to their shopping cart
                        Order.orderProductControl(currentUser, in, out);
                        break;
                    case "2":
                        //View shopping cart
                        Order.myShoppingCartAndGroup(currentUser, in, out);
                        break;
                    case "3":
                        // Back to main page
                        System.out.println("User choose to back to main page.");
                        out.println("Back to main menu.");
                        restart = false;
                        break;
                    case "4":
                        // Exit
                        // Leaving the system
                        out.println("You are now exiting the system.");
                        out.println("Are you sure to do so? (y/n)");
                        if (Toolkit.askContinue(in, out)) {
                            out.println("Goodbye!");
                        }
                        break;
                    default:
                        out.println("Invalid option, please try again.");
                }
            } catch (IOException e){
                out.println("Something went wrong. Please try again.");
            }
        }
    }

    // Show products (viewProductList methods) (Next page methods)
    private static void showProducts(Data.User currentUser, int page, BufferedReader in, PrintWriter out) {
        int startIndex = (page - 1) * PRODUCTS_PER_PAGE;
        String productDisplay = menuPageDEMO(page);
        // No available products
        if (productDisplay.isEmpty()) {
            out.println("No products available :(");
            return;
        }
        out.println("\n----- Products (Page " + page + ")-------");
        out.println(productDisplay);

        out.println("\nEnter 'Next' for next page, or 'Prev' for previous page. 'Add/Remove' to manage cart, or 'Back' to return to menu ");

        try {
            String input = in.readLine();
            if ("Next".equalsIgnoreCase(input)) {
                currentPage++;  // Next
            } else if ("Prev".equalsIgnoreCase(input) && currentPage > 1) {
                currentPage--;  //Prev
            } else if ("Back".equalsIgnoreCase(input)) {
                return;  //Back
            } else if ("Add/Remove".equalsIgnoreCase(input)) {
                // Call order method
                Order.orderProductControl(currentUser, in, out);
            } else {
                out.println("Invalid input: " + input);
            }

            if (!"Back".equalsIgnoreCase(input)) {
                showProducts(currentUser, currentPage, in, out);
            }

        } catch (IOException e) {
            out.println("Something went wrong. Please try again.");
            System.err.println("Error reading input: " + e.getMessage());
        }
    }

    private static String menuPageDEMO(int page) {
        // show all products from database
        int offset = (page - 1) * PRODUCTS_PER_PAGE;
        try (Connection connection = new Service().getConnection();
             Statement statement = connection.createStatement();
             // in descending order of product_id
             ResultSet resultSet = statement.executeQuery("SELECT * FROM products ORDER BY product_id DESC LIMIT " + PRODUCTS_PER_PAGE + " OFFSET " + offset)) {
            //ResultSet resultSet = statement.executeQuery("SELECT * FROM products") { //Need to show the id as well or otherwise user cannot add/remove items
            StringBuilder sb = new StringBuilder();
            sb.append("Product ID | Product Name | Price | Discount | Availability\n");
            while (resultSet.next()) {
                sb.append(resultSet.getInt("product_id")).append(" | ")
                        .append(resultSet.getString("product_name")).append(" | ")
                        .append(resultSet.getDouble("product_price")).append(" | ")
                        .append(resultSet.getDouble("discount")).append(" | ")
                        .append(resultSet.getBoolean("is_available") ? "in stock" : "out of stock").append("\n");
            }

            return sb.toString();

        } catch (SQLException e) {
            System.err.println("An error occurred: " + e.getMessage());
            return "Error accessing database.";
        }
    }
}