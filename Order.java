package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.UUID;

public class Order {
    private static Data.Group currentGroup = null;

    public static void myShoppingCartAndGroup(Data.User currentUser, BufferedReader in, PrintWriter out) throws IOException, SQLException {
        //TODO may be bring connection to here and pass as parameter to methods

        boolean restart = true;

        while (restart) {
            out.println("===== My Shopping Cart and Group =====");
            out.println("1. My Shopping Cart"); // This will show all items in the order
            out.println("2. My Group"); // This will show all member in the groups
            out.println("3. Back to Menu");
            out.println("Choose an option: ");

            try {
                String option = in.readLine();
                switch (option) {
                    case "1":
                        while (true) {
                            //View items in shopping cart(order)
                            showShoppingCart(currentUser, in, out);
                            out.println("You can add or remove items from your shopping cart.");
                            // clear all input
                            out.println("Enter 'Edit' to edit your order, 'Checkout' to proceed to payment, or enter 'Back' to return to previous page.");
                            String userInput = in.readLine();
                            if (userInput.equalsIgnoreCase("Edit")) {
                                orderProductControl(currentUser, in, out);
                            } else if (userInput.equalsIgnoreCase("Checkout")) {
                                Menu.checkoutPage(currentUser, in, out);
                                out.println("Returning to Menu...");
                                break;
                            } else if (userInput.equalsIgnoreCase("Back")) {
                                break;
                            } else {
                                out.println("Invalid option, please try again.");
                            }
                        }
                        break;
                    case "2":
                        // Show user's group
                        showGroup(currentUser, in, out);
                        break;
                    case "3":
                        // Back to main page
                        System.out.println("User choose to back to main page.");
                        out.println("Back to main page.");
                        restart = false;
                        break;
                    default:
                        out.println("Invalid option, please try again.");
                }
            } catch (IOException e) {
                out.println("Something went wrong. Please try again.");
            }
        }

    }

    /*
     * This method will show all items in the order(s)
     * If the user is in a group, show all orders in the group
     * If the user is not in a group, show all items in the user's own order
     */

    private static void showShoppingCart(Data.User currentUser, BufferedReader in, PrintWriter out) throws IOException {
        String singleUserCartQuery = "SELECT ROW_NUMBER() over (ORDER BY items.product_id) AS NumOfItems, items.product_id, products.product_name, items.quantity, items.total_price FROM items " +
                "INNER JOIN products ON products.product_id = items.product_id " +
                "WHERE items.order_id = ? ";

        // show all group member's order
        String groupCartQuery = "SELECT ROW_NUMBER() over (ORDER BY items.product_id) AS NumOfItems, items.product_id, products.product_name, items.quantity, items.total_price, users.user_name FROM items " +
                "INNER JOIN products ON products.product_id = items.product_id " +
                "INNER JOIN public.orders ON items.order_id = orders.order_id " +
                "INNER JOIN public.groups ON orders.group_id = groups.group_id " +
                "INNER JOIN public.users ON orders.user_id = users.user_id " +
                "WHERE groups.group_id = ? AND orders.order_status = ?";

        // show all items in user's own order
        try (Connection connection = new Service().getConnection();
             PreparedStatement singleUserStmt = connection.prepareStatement(singleUserCartQuery);
             PreparedStatement groupStmt = connection.prepareStatement(groupCartQuery)
        ) {

            if (isInGroup(connection, currentUser)) {
                // if user is in a group, show all items in the group
                if (currentGroup == null) {
                    currentGroup = getTargetGroup(currentUser);
                }
                if (currentGroup != null) {
                    groupStmt.setInt(1, currentGroup.getGroup_id());
                    groupStmt.setString(2, "Created");
                    ResultSet resultSet = groupStmt.executeQuery();

                    out.println("========= Shopping Cart =========");
                    out.println("Item No. | Product ID | Product Name | Quantity | Total Price | Added By");
                    out.println("---------|------------|--------------|----------|------------|----------");

                    if (!resultSet.next()) {
                        out.println("Your shopping cart is empty. Maybe you want to ...");
                    } else {
                        do {
                            out.println(resultSet.getInt("NumOfItems") + "       | "
                                    + resultSet.getString("product_id") + "     | "
                                    + resultSet.getString("product_name") + " | "
                                    + resultSet.getString("quantity") + " | "
                                    + resultSet.getString("total_price") + " | "
                                    + resultSet.getString("user_name"));
                        } while (resultSet.next());
                    }
                }
            } else {
                int order_id = getTargetOrderID(currentUser);
                singleUserStmt.setInt(1, order_id);
                ResultSet resultSet = singleUserStmt.executeQuery();

                out.println("========= Shopping Cart =========");
                out.println("Item No. | Product ID | Product Name | Quantity | Total Price");
                out.println("---------|------------|--------------|----------|------------");

                if (!resultSet.next()) {
                    out.println("Your shopping cart is empty. Maybe you want to ...");
                } else {
                    do {
                        out.println(resultSet.getInt("NumOfItems") + "       | "
                                + resultSet.getString("product_id") + "     | "
                                + resultSet.getString("product_name") + " | "
                                + resultSet.getString("quantity") + " | "
                                + resultSet.getString("total_price"));
                    } while (resultSet.next());
                }
            }
        } catch (SQLException e) {
            System.err.println("An error occurred: " + e.getMessage());
            out.println("showItemInfo: Error accessing database.");
        }
    }

    // My group
    public static void showGroup(Data.User currentUser, BufferedReader in, PrintWriter out) throws IOException {
        try (Connection connection = new Service().getConnection()) {
            while (true) {
                // check if user is in a group
                if (isInGroup(connection, currentUser)) {
                    // if user is in a group, then show the group
                    if (currentGroup == null) {
                        currentGroup = getTargetGroup(currentUser);
                    }
                    if (currentGroup != null) {
                        String groupName = currentGroup.getGroup_name();
                        out.println("You are now in " + groupName);
                        showGroupMembers(currentUser, in, out);
                        out.println("You can leave the group by entering 'Leave'. Or enter 'Back' to return to previous page.");
                        String userInput = in.readLine();
                        if (userInput.equalsIgnoreCase("Leave")) {
                            leaveGroup(connection, currentUser, in, out);
                        } else if (userInput.equalsIgnoreCase("Back")) {
                            break;
                        } else {
                            out.println("Invalid option, please try again.");
                        }
                    }
                } else {
                    // if user is not in a group, then user can join a group or create a new group
                    showAllGroup(currentUser.getHouse_id(), in, out);
                    out.println("You are not in a group yet. You can: ");
                    out.println("1. Join a group");
                    out.println("2. Create a new group");
                    out.println("3. Back to Menu");
                    String groupOption = in.readLine();
                    switch (groupOption) {
                        case "1":
                            joinGroup(connection, currentUser, in, out);
                            break;
                        case "2":
                            createGroup(connection, currentUser, in, out);
                            break;
                        case "3":
                            return; //back to My Shopping Cart and Group page
                        default:
                            out.println("Invalid option. Please try again.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("An error occurred: " + e.getMessage());
            out.println("Error while showing shopping cart.");
        }
    }

    /* Search from all orders of the user
       If there is an active order, return the order_id */
    public static int getTargetOrderID(Data.User currentUser) {
        String getOrderIDQuery = "SELECT order_id FROM orders WHERE user_id = ?::uuid AND order_status = ?";

        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement(getOrderIDQuery)) {
            statement.setObject(1, currentUser.getUser_id());
            statement.setString(2, "Created");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                System.out.println("Order found: " + resultSet.getInt("order_id"));
                return resultSet.getInt("order_id");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred: " + e.getMessage());
            // trace the error
        }
        // If there is no active order, return -1
        System.out.println("getTargetOrderID: No order is found.");
        return -1;
    }

    /*
     * This method will call addProduct() and removeProduct() methods
     * Do not call addProduct and removeProduct directly, call this method instead
     */
    public static void orderProductControl(Data.User currentUser, BufferedReader in, PrintWriter out) {
        // get all products information from database
        // <Add> <product_id> to add products. <Remove> <product_id> to remove products.
        out.println("Enter 'Add' <product_id> to add products. Enter 'Remove' <product_id> to remove products.");
        out.println("e.g. Add 300001 / Remove 300002");
        out.println("Enter '\\d' to finish adding products.");

        int targetOrderID = getTargetOrderID(currentUser);

        // The system will create an order when user goes to the menu
        // if user do not have active order, create one
        if (targetOrderID == -1) {
            System.out.println("orderProductControl: User does not have an active order. Creating one now.");
            try {
                createOrder(currentUser);
                targetOrderID = getTargetOrderID(currentUser);
            } catch (SQLException e) {
                System.err.println("orderProductControl Error: " + e.getMessage());
            }
        } else {
            System.out.println("displayMenu: User already have a order.");
        }

        while (true) {
            try {
                String userCommand = in.readLine();
                String[] command = Toolkit.parseInput(userCommand);
                if (command.length == 2 && command[0].equalsIgnoreCase("Add")) {
                    // Add product ID to a list.
                    if (Toolkit.isInteger(command[1])) {
                        // add product to order
                        int productID = Integer.parseInt(command[1]);
                        addProduct(productID, targetOrderID, in, out);
                    } else {
                        out.println("Invalid product ID. Please try again.");
                        out.println("eg. Add 300001 / Remove 300002");
                    }

                } else if (command.length == 2 && command[0].equalsIgnoreCase("Remove")) {
                    // remove item from order
                    // check from prepProducts list first
                    if (Toolkit.isInteger(command[1])) {
                        int productID = Integer.parseInt(command[1]);
                        // remove product from order
                        removeProduct(productID, targetOrderID, in, out);
                    } else {
                        out.println("Invalid product ID. Please try again.");
                        out.println("eg. Add 300001 / Remove 300002");
                    }


                } else if (command.length == 1 && command[0].equalsIgnoreCase("\\d")) {
                    break;

                } else {
                    out.println("Invalid input. Please try again. Or enter '\\d' to finish.");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // clean up the order
        // if the order includes items with quantity 0, remove the item
        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM items WHERE quantity = 0")) {
            System.out.println("Removing all items with quantity 0.");
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("orderProductControl Error: " + e.getMessage());
        }
    }

    /*
     * set up a new order
     * a user may have multiple orders but can only have one active order whose status is "Created"
     */
    private static void addProduct(int productID, int targetOrderID, BufferedReader in, PrintWriter out) throws IOException {
        // send query to database to check if product exists and available
        // if exists, add to order
        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM products WHERE product_id = ?")) {
            statement.setInt(1, productID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                // create a product object
                // the product must exist in the database
                Data.Product product = new Data.Product(resultSet.getInt("product_id"),
                        resultSet.getString("product_name"),
                        resultSet.getDouble("product_price"),
                        resultSet.getDouble("discount"),
                        resultSet.getInt("supplier_id"),
                        resultSet.getBoolean("is_available"));

                // A user may have multiple order but can only have one active order
                // only add if product is available
                if (product.isAvailable) {
                    // Check if the item is already in the order
                    PreparedStatement checkItemStmt = connection.prepareStatement("SELECT items.item_id, quantity FROM items WHERE order_id = ? AND product_id = ?");
                    checkItemStmt.setInt(1, targetOrderID);
                    checkItemStmt.setInt(2, productID);
                    ResultSet itemResult = checkItemStmt.executeQuery();

                    if (itemResult.next()) {
                        // If the product is already in the order, which means order id has been assigned
                        // update the quantity + 1
                        int newQuantity = itemResult.getInt("quantity") + 1;
                        int itemID = itemResult.getInt("item_id");

                        PreparedStatement updateItemStmt = connection.prepareStatement("UPDATE items SET quantity = ?, total_price = ? WHERE order_id = ? AND item_id = ? AND product_id = ?");
                        updateItemStmt.setInt(1, newQuantity);
                        updateItemStmt.setDouble(2, product.product_price * newQuantity);
                        updateItemStmt.setInt(3, targetOrderID); // assign the item to the target order
                        updateItemStmt.setInt(4, itemID);
                        updateItemStmt.setInt(5, productID);
                        updateItemStmt.executeUpdate();

                    } else {
                        // If the product is not in the order, assign the order id, insert it, set quantity as 1
                        PreparedStatement addProductStmt = connection.prepareStatement("INSERT INTO items (order_id, product_id, quantity, total_price) VALUES (?, ?, ?, ?)");
                        addProductStmt.setInt(1, targetOrderID); // assign the item to the target order
                        addProductStmt.setInt(2, productID);
                        addProductStmt.setInt(3, 1);
                        addProductStmt.setDouble(4, product.product_price);
                        addProductStmt.executeUpdate();
                    }
                    out.println("You have added 1 " + product.product_name + " to your cart.");
                    out.println("You can continue to add more items or enter '\\d' to finish.");
                } else {
                    out.println("Product is not available.");
                }

            } else {
                // if not, print error message
                out.println("Product not found. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("addProduct Error: " + e.getMessage());
            out.println("Error accessing database.");
        }
    }

    private static void removeProduct(int productID, int targetOrderID, BufferedReader in, PrintWriter out) throws IOException {
        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM products WHERE product_id = ?")) {
            statement.setInt(1, productID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                // the product must exist in the database
                Data.Product product = new Data.Product(resultSet.getInt("product_id"),
                        resultSet.getString("product_name"),
                        resultSet.getDouble("product_price"),
                        resultSet.getDouble("discount"),
                        resultSet.getInt("supplier_id"),
                        resultSet.getBoolean("is_available"));

                // remove product from sub_order table

                // if items exist, remove the item -1 each time
                // Check if the item is already in the order
                PreparedStatement checkItemStmt = connection.prepareStatement("SELECT quantity FROM items WHERE order_id = ? AND product_id = ?");
                checkItemStmt.setInt(1, targetOrderID);
                checkItemStmt.setInt(2, productID);
                ResultSet itemResult = checkItemStmt.executeQuery();

                if (itemResult.next()) {
                    // If the product is already in the order, update the quantity - 1
                    if (itemResult.getInt("quantity") == 1) {
                        // if the quantity is 1, remove the item
                        PreparedStatement removeItemStmt = connection.prepareStatement("DELETE FROM items WHERE order_id = ? AND product_id = ?");
                        removeItemStmt.setInt(1, targetOrderID); // assign the item to the target order
                        removeItemStmt.setInt(2, productID);
                        removeItemStmt.executeUpdate();
                    } else {
                        int newQuantity = itemResult.getInt("quantity") - 1;
                        PreparedStatement updateItemStmt = connection.prepareStatement("UPDATE items SET quantity = ?, total_price = ? WHERE order_id = ? AND product_id = ?");
                        updateItemStmt.setInt(1, newQuantity);
                        updateItemStmt.setDouble(2, product.product_price * newQuantity);
                        updateItemStmt.setInt(3, targetOrderID); // assign the item to the target order
                        updateItemStmt.setInt(4, productID);
                        updateItemStmt.executeUpdate();
                    }

                    out.println("You have removed 1 " + product.product_name + " from your cart.");

                } else {
                    // If the product is not in the order
                    out.println("Product not found in order. Please try again.");
                }

                out.println("Product removed from order. You can continue to remove more items or enter '\\d' to finish.");

            } else {
                // if not, print error message
                out.println("You didn't input a valid product ID. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("removeProduct error: " + e.getMessage());
        }
    }

    /* Check if user is in a group
       if the user's order has been assigned to a group, then the user is in a group */
    private static boolean isInGroup(Connection connection, Data.User currentUser) {
        String inGroupQuery = "SELECT * FROM orders WHERE user_id = ?::uuid AND is_in_group = ? AND order_status = ?";

        try (PreparedStatement statement = connection.prepareStatement(inGroupQuery)) {
            statement.setObject(1, currentUser.getUser_id());
            statement.setBoolean(2, true);
            statement.setString(3, "Created");
            ResultSet resultSet = statement.executeQuery();

            // DEBUG: see if resultSet return anything
            System.out.println("isInGroup: resultSet: " + resultSet);

            if (resultSet.next()) {
                System.out.println("isInGroup: User " + currentUser.getUser_id() + " is in a group.");
                return true;
            } else {
                System.out.println("isInGroup: User " + currentUser.getUser_id() + " is not in a group.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("isInGroup error: An error occurred: " + e.getMessage());
        }
        return false;
    }

    /*
     * Since one user can only have one active group and one order, the target is persistent
     * we can get the order_id by searching the database
     * From group -> orders -> user
     */
    public static Data.Group getTargetGroup(Data.User currentUser) {
        // TODO: Bugs this method only return group when the user is the owner
        String getGroupQuery = "SELECT groups.user_id AS owner_id, " +
                "groups.group_name AS group_name, " +
                "groups.group_id AS group_id, " +
                "orders.order_status " +
                "FROM groups " +
                "JOIN orders ON groups.group_id = orders.group_id " +
                "WHERE orders.user_id = ?::uuid AND orders.order_status = ?";

        // Search from all orders of the user
        // If there is an active order, return the order_id
        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement(getGroupQuery)) {

            statement.setObject(1, currentUser.getUser_id());
            statement.setString(2, "Created");
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Group found: " + resultSet.getInt("group_id"));
                int groupID = resultSet.getInt("group_id");
                String groupName = resultSet.getString("group_name");
                UUID ownerID = resultSet.getObject("owner_id", UUID.class);
                // System.out.println("DEBUG: Group ID: " + groupID);
                return new Data.Group(groupID, groupName, ownerID);
            } else {
                // return null if no group is found
                System.out.println("getTargetGroup: No group is found.");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error when getTargetGroup: " + e.getMessage());
            return null;
        }
    }


    /*
     * Warning: This method shall not be called directly or be seen as an option by users.
     * This method should be called from leaveGroup() method only, when user is trying to leave a group they created.
     */
    public static void dismissGroup(Data.Group targetGroup) {
        String updateOrderQuery = "UPDATE orders SET is_in_group = false WHERE group_id = ?";
        String deleteGroupQuery = "DELETE FROM groups WHERE group_id = ?";
        int targetGroupID = targetGroup.getGroup_id();

        try (Connection connection = new Service().getConnection();
             //Set is_in_group to false for all orders in the group
             PreparedStatement updateGroup = connection.prepareStatement(updateOrderQuery)) {
            updateGroup.setInt(1, targetGroupID);
            updateGroup.executeUpdate();

            // delete the group from groups table
            // because the group no longer exists
            PreparedStatement deleteGroup = connection.prepareStatement(deleteGroupQuery);
            deleteGroup.setInt(1, targetGroupID);
            deleteGroup.executeUpdate();

        } catch (SQLException e) {
            System.err.println("dismissGroup Error: " + e.getMessage());
        }
    }


    /*
     * This method will show all group members where the user is in
     */
    private static void showGroupMembers(Data.User currentUser, BufferedReader in, PrintWriter out) {
        String groupUserQuery = "SELECT users.user_name AS username FROM users " +
                "JOIN orders ON users.user_id = orders.user_id " +
                "WHERE orders.group_id = ? AND orders.is_in_group = true";

        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement(groupUserQuery)) {

            if (currentGroup == null) {
                currentGroup = getTargetGroup(currentUser);
            }
            if (currentGroup != null) {
                int groupID = currentGroup.getGroup_id();
                System.out.println("DEBUG: Group ID: " + groupID);
                statement.setInt(1, groupID);
                ResultSet resultSet = statement.executeQuery();

                out.println("========= Group Members =========");
                out.println("User Name");
                out.println("---------");

                while (resultSet.next()) {
                    System.out.println("DEBUG: User Name: " + resultSet.getString("username")); // not showing
                    out.println(resultSet.getString("username"));
                }
            }
        } catch (SQLException e) {
            System.err.println("showGroupMembers Error: " + e.getMessage());
        }
    }

    //Amina
    //Total price
    public static double calculateTotalPrice(int orderId) throws SQLException {
        String query = "SELECT SUM(total_price * quantity) AS total_price FROM items WHERE order_id = ?";
        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, orderId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("total_price");
            } else {
                return 0.0; // No items in the order
            }
        }
    }

    public static double calculateTotalPrice(Data.User currentUser) throws SQLException {
        int orderId = getTargetOrderID(currentUser); // Retrieve the order ID for the user
        if (orderId == -1) {
            return 0.0; // No active order
        }
        return calculateTotalPrice(orderId); // Call the other method
    }

    //Amin
    //Complete Order
    public static void completeOrder(int orderID) throws SQLException {
        String query = "UPDATE orders SET order_status = 'Completed' WHERE order_id = ?";
        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, orderID);
            statement.executeUpdate();
        }
    }

    /*
     * Show all groups within the house user lives in
     * include active groups only (Can join)
     */
    private static void showAllGroup(int house_id, BufferedReader in, PrintWriter out) {
        String showGroupQuery = "SELECT groups.group_id, groups.group_name, creator.user_name AS created_user, " +
                "string_agg( DISTINCT joined.user_name, ', ') AS joined_users FROM groups " +
                "INNER JOIN orders on orders.group_id = groups.group_id " +
                "INNER JOIN users creator ON groups.user_id = creator.user_id " +
                "INNER JOIN users joined ON orders.user_id = joined.user_id " +
                "WHERE groups.house_id = ? AND groups.order_status = ? " +
                "GROUP BY groups.group_id, groups.group_name, creator.user_name " +
                "ORDER BY groups.group_id; ";

        //show all groups within the house user lives in
        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement(showGroupQuery)) {
            statement.setInt(1, house_id);
            statement.setString(2, "Created");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                out.println("Group ID | Group Name | Created by | Joined Users");
                out.println("---------|------------|------------|-------------");

                while (resultSet.next()) {
                    out.println(resultSet.getInt("group_id") + "   | "
                            + resultSet.getString("group_name") + " | "
                            + resultSet.getString("created_user") + " | "
                            + resultSet.getString("joined_users"));
                }
            } else {
                out.println("You don't have any group yet.");
                out.println("Maybe you can create a new group.");
                out.println("Returning to previous page...");
            }

        } catch (SQLException e) {
            System.err.println("An error occurred: " + e.getMessage());
            out.println("showOrderInfo: Error accessing database.");
        }
    }

    private static void leaveGroup(Connection connection, Data.User currentUser, BufferedReader in, PrintWriter out) {
        try {
            if (isInGroup(connection, currentUser)) {
                // ask user to confirm
                out.println("Are you sure you want to leave the group? (y/n)");
                if (Toolkit.askContinue(in, out)) {
                    if (currentGroup == null) {
                        currentGroup = getTargetGroup(currentUser);
                    }
                    if (currentGroup != null) {
                        UUID ownerID = currentGroup.getOwner_id();

                        System.out.println("DEBUG: The owner of the group is: " + ownerID); // Incorrect
                        System.out.println("DEBUG: The current user is: " + currentUser.getUser_id());

                        // if user is the owner of the group, then print error message
                        if (currentUser.getUser_id().equals(ownerID)) {
                            out.println("You are now trying to leave a group you created.");
                            out.println("Continue to do so means you will dismiss the group.");
                            out.println("Once you have dismissed the group, all members will keep their orders but the group will be deleted.");
                            out.println("Are you sure to continue? (y/n)");

                            if (Toolkit.askContinue(in, out)) {
                                dismissGroup(currentGroup);
                                out.println("You have dismissed the group.");
                            } else {
                                out.println("Returning to previous page...");
                            }
                        } else {
                            // if user is in a group, then leave the group
                            PreparedStatement statement = connection.prepareStatement("UPDATE orders SET group_id = NULL, is_in_group = false WHERE user_id = ?::uuid AND is_in_group = ?");
                            statement.setObject(1, currentUser.getUser_id());
                            statement.setBoolean(2, true);
                            statement.executeUpdate();
                        }
                        out.println("You have left the group.");
                        currentGroup = null;
                    }
                }
            } else {
                // if user is not in a group, then print error message
                out.println("You are not in a group.");
            }
        } catch (SQLException e) {
            System.out.println("leaveGroup Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Join Group Method
    private static void joinGroup(Connection connection, Data.User currentUser, BufferedReader in, PrintWriter out) {
        try {
            while (true) {
                out.println("Enter group ID to join (or \\q to quit): ");
                String groupId = in.readLine();
                if ("\\q".equalsIgnoreCase(groupId)) {
                    out.println("Returning to previous page...");
                    break;
                } else {
                    if (!Toolkit.isInteger(groupId)) {
                        out.println("Invalid group ID. Please try again.");
                        continue;
                    }
                    // Check if the group exists
                    // search from the group table
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM groups WHERE group_id = ?");
                    statement.setInt(1, Integer.parseInt(groupId));
                    ResultSet resultSet = statement.executeQuery();
                    if (!resultSet.next()) {
                        out.println("Group not found. Please try again.");
                        return;
                    }

                    // assign user's own order to a group
                    PreparedStatement updateOrder = connection.prepareStatement("UPDATE orders SET is_in_group = ? , group_id = ? WHERE user_id = ?::uuid AND order_status = ?");
                    updateOrder.setBoolean(1, true);
                    updateOrder.setInt(2, Integer.parseInt(groupId));
                    updateOrder.setObject(3, currentUser.getUser_id());
                    updateOrder.setString(4, "Created");

                    updateOrder.executeUpdate();

                    System.out.println("User " + currentUser.getUser_id() + "joined group: " + groupId);
                    out.println("You have successfully joined group: " + groupId);
                    break;
                }
            }
        } catch (IOException e) {
            out.println("Something went wrong. Please try again.");
            System.err.println("Error reading input: " + e.getMessage());
        } catch (SQLException e) {
            out.println("Something went wrong. Please try again.");
            System.err.println("Error reading input: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /*
     * This method can only add one order into the database
     * It doesn't check if the user has already created an order
     * And it doesn't check if the user has already joined an order
     */
    private static void createGroup(Connection connection, Data.User currentUser, BufferedReader in, PrintWriter out) {
        // Add a new group to the database
        System.out.println("Creating a new Group for user: " + currentUser.getUser_id());
        if (isInGroup(connection, currentUser)) {
            System.out.println("Stop, user is already in a group: " + isInGroup(connection, currentUser));
            out.println("You are already in a group.");
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO groups (group_name, order_status, user_id, house_id) VALUES (?, ?, ?::uuid, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            // the name of the order will be generated base on their name
            String groupName = currentUser.getUser_name() + "'s Group";
            statement.setString(1, groupName);
            statement.setString(2, "Created");
            statement.setObject(3, currentUser.getUser_id());
            statement.setInt(4, currentUser.getHouse_id());
            statement.executeUpdate();

            // assign the user's active order to the group just created
            ResultSet generatedKeys = statement.getGeneratedKeys();

            if (generatedKeys.next()) {
                PreparedStatement updateOrder = connection.prepareStatement("UPDATE orders SET group_id = ?, is_in_group = ? WHERE user_id = ?::uuid AND order_status = ?");
                System.out.println("Group ID: " + generatedKeys.getInt(1));
                int groupID = generatedKeys.getInt(1);

                System.out.println(">===== Group ID: " + groupID);
                updateOrder.setInt(1, groupID);
                updateOrder.setBoolean(2, true); // set the order to be in a group
                updateOrder.setObject(3, currentUser.getUser_id());
                updateOrder.setString(4, "Created");
                int rowsUpdated = updateOrder.executeUpdate();

                System.out.println("Rows updated: " + rowsUpdated);
                System.out.println("Order.createGroup: Group created. Group name: " + groupName);
                out.println("You have created a group. Group name: " + groupName + " | Group ID: " + groupID);
            } else {
                throw new SQLException("Creating group failed, no ID obtained.");
            }


        } catch (SQLException e) {
            System.err.println("createGroup Error: " + e.getMessage());
            out.println("createGroup Error: Error accessing database.");
        }
    }


    /*
     * This method can only add one sub_order into the database
     * It doesn't check if the user has already created an order
     * One user can only have one active sub_order
     * When creating an order, you will need to give a targetOrderID
     */
    static void createOrder(Data.User currentUser) throws SQLException {
        // check if user already has an active order
        // if user already in an order, then not allowed to create a new order
        // check if user have an active order
        try (Connection connection = new Service().getConnection();
             PreparedStatement checkIfInOrder = connection.prepareStatement("SELECT * FROM orders WHERE user_id = ?::uuid AND order_status = ?");) {
            checkIfInOrder.setObject(1, currentUser.getUser_id());
            checkIfInOrder.setString(2, "Created");
            ResultSet resultSet = checkIfInOrder.executeQuery();

            if (resultSet.next()) {
                // already in an active order, not allowed to create a new order
                System.out.println("User" + currentUser.getShort_id() + " already in an order.");
                System.out.println("We don't create an order for: " + currentUser.getShort_id());
                // if user already in an order, we don't create a new order
            } else {
                //Create new order for the user
                System.out.println("Creating a new order for user: " + currentUser.getUser_id());
                Data.Order Order = new Data.Order(currentUser);

                PreparedStatement statement = connection.prepareStatement("INSERT INTO orders (user_id, order_status,is_in_group) VALUES (?, ?, ?)");
                statement.setObject(1, currentUser.getUser_id());
                statement.setString(2, "Created"); // set order status as created
                statement.setBoolean(3, false); //set is_in_group to false
                statement.executeUpdate();
                System.out.println("Order.createOrder: Order created for user.");
            }
        } catch (SQLException e) {
            System.err.println("createOrder Error: " + e.getMessage());
        }
    }

}