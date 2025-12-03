package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Menu {
    // List of Products
    private static int currentPage;
    private static final int PRODUCTS_PER_PAGE = 5;

    //Possible Load sample product

    //Display Menu
    public static void displayMenu(Data.User currentUser, BufferedReader in, PrintWriter out) throws SQLException {
        // when showing the shopping cart, we are actually showing
        // 1. if user in a group, then show their own order
        // 2. if user not in a group, then show all groups in the house

        boolean restart = true;
        while (restart) {
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
                        currentPage = 1; //Set currentPage to 1 so every time the menu is shown from the first page
                        showProducts(currentUser, currentPage, in, out);
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
            } catch (IOException e) {
                out.println("Something went wrong. Please try again.");
            }
        }
    }

    // Show products (viewProductList methods) (Next page methods)
    private static void showProducts(Data.User currentUser, int page, BufferedReader in, PrintWriter out) {
        String productDisplay = productPage(page);
        // No available products
        if (productDisplay.isEmpty()) {
            out.println("No products available :(");
            return;
        }
        out.println("\n----- Products (Page " + page + ") -------");
        out.println(productDisplay);

        out.println("Enter 'Next' for next page, or 'Prev' for previous page. 'Back' to return to menu ");
        out.println("Enter 'Edit' to add or remove items from your shopping cart.");

        try {
            String input = in.readLine();
            if ("Next".equalsIgnoreCase(input)) {
                currentPage++;  // Next
            } else if ("Prev".equalsIgnoreCase(input)) {
                if (currentPage > 1) {
                    currentPage--;  //Prev
                } else {
                    out.println("This is the first page.");
                }
            } else if ("Back".equalsIgnoreCase(input)) {
                return;  //Back
            } else if ("Edit".equalsIgnoreCase(input)) {
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

    public static void checkoutPage (Data.User currentUser, BufferedReader in, PrintWriter out) throws SQLException {
        //Any active order
        int orderId = Order.getTargetOrderID(currentUser);
        if (orderId == -1) {
            out.println("No active orders. Add items to cart before proceeding to checkout!");
            return;
        }
        //Total Price
        double totalPrice = Order.calculateTotalPrice(orderId);
        out.println("\n===== Checkout Page =====");
        out.println("The total price of your order is £" + String.format("%.2f", totalPrice));
        out.println("Select your payment method from the following:");
        out.println("1. Card");
        out.println("2. PayPal");
        out.println("3. Apple Pay");

        try {
            // Loop until a valid payment method is chosen
            String choice = "";
            boolean validChoice = false;

            while (!validChoice) {
                out.print("Enter your payment method: ");
                choice = in.readLine();

                switch (choice) {
                    case "1":
                    case "2":
                    case "3":
                        validChoice = true;
                        out.println("Processing Payment... Please wait.");  // Simulating payment processing
                        Thread.sleep(2000); // Simulating delay for payment processing
                        break;
                    default:
                        out.println("Invalid payment method. Please choose again.");
                        break;
                }
            }
            Order.completeOrder(orderId);
            out.println("Payment successful. Thanks for shopping at SSH Grocery! :D");

            } catch (IOException | InterruptedException e) {
            out.println("An error occurred during checkout. Please try again.");
            System.err.println("Error in checkoutPage: " + e.getMessage());
        }
    }

    private static String productPage(int page) {
        // show all products from database
        int offset = (page - 1) * PRODUCTS_PER_PAGE;
        String getProductQuery = "SELECT * FROM products ORDER BY product_id LIMIT " + PRODUCTS_PER_PAGE + " OFFSET " + offset;

        try (Connection connection = new Service().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(getProductQuery)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Product ID | Product Name | Price | Discount | Availability\n");
            sb.append("-----------|--------------|-------|----------|-------------\n");
            while (resultSet.next()) {
                sb.append(resultSet.getInt("product_id")).append("     | ")
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