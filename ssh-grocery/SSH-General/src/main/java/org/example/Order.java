package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Objects;
import java.util.UUID;

public class Order {
    public static void myShoppingCartAndGroup(Data.User currentUser, BufferedReader in, PrintWriter out) throws IOException {
        //TODO may be bring connection to here and pass as parameter to methods
        boolean restart = true;
        while (restart) {
            out.println("===== My Shopping Cart and Group =====");
            out.println("1. My Shopping Cart"); // This will show all items in the order
            out.println("2. My Group"); // This will show all groups in the house
            out.println("3. Back to Menu");
            out.println("4. Exit");

            try {
                String option = in.readLine();
                switch (option) { // TODO What about wrapping the blocks into methods
                    case "1":
                        while (true) {
                            //View items in shopping cart(order)
                            showItemInfo(getTargetOrderID(currentUser), in, out);
                            // TODO Before this, we need to check if user is in a group in showShoppingCart()
                            out.println("You can add or remove items from your shopping cart.");
                            // clear all input
                            out.println("Enter 'Edit' to edit your order. Or enter 'Back' to return to previous page.");
                            String userInput = in.readLine();
                            if (userInput.equalsIgnoreCase("Edit"))
                                // Edit order
                                orderProductControl(currentUser, in, out);
                            else if (userInput.equalsIgnoreCase("Back")) // TODO back sometimes not work
                                break;
                            else
                                out.println("Invalid option."); //TODO may need a better way to handle invalid input
                        }
                        break;
                    case "2":
                        // My group
                        // check if user is in a group
                        try (Connection connection = new Service().getConnection()) {
                            if (isInGroup(connection, currentUser)) {
                                // if user is in a group, then show the group
                                out.println("You are now in " + Objects.requireNonNull(getTargetGroup(currentUser)).getGroup_name());
                                showGroupMembers(currentUser, in, out);
                                out.println("You can leave the group by entering 'Leave'. Or enter 'Back' to return to previous page.");
                                if (in.readLine().equalsIgnoreCase("Leave")) {
                                    leaveGroup(connection, currentUser, in, out);
                                } else if (in.readLine().equalsIgnoreCase("Back")){
                                    break;
                                } else {
                                    out.println("Invalid option."); //TODO may need a better way to handle invalid input
                                    // restart = false;
                                }

                            } else {
                                // if user is not in a group, then user can join a group or create a new group
                                //TODO show the groups in the house
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
                                        restart = false;
                                        break;
                                    default:
                                        out.println("Invalid option. Please try again.");
                                }
                            }
                        } catch (SQLException e) {
                            System.err.println("An error occurred: " + e.getMessage());
                            out.println("Error while showing shopping cart.");
                        }
                        break;
                    case "3":
                        // Back to main page
                        System.out.println("User choose to back to main page.");
                        out.println("Back to main page.");
                        restart = false;
                        break;
                    case "4":
                        // Exit
                        // Back to main menu
                        out.println("Back to main menu.");
                        System.exit(0);
                        break;
                    default:
                        out.println("Invalid option, please try again.");
                }
            } catch (IOException e) {
                out.println("Something went wrong. Please try again.");
            }
        }

    }

    /* Search from all orders of the user
       If there is an active order, return the order_id */
    public static int getTargetOrderID(Data.User currentUser) {
        String query = "SELECT order_id FROM orders WHERE user_id = ?::uuid AND order_status = ?";

        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
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

    //Show all items in the order, simply refers to shopping cart
    private static void showItemInfo(int order_id, BufferedReader in, PrintWriter out) throws IOException {
        String query = "SELECT ROW_NUMBER() over (ORDER BY items.product_id) AS NumOfItems, items.product_id, products.product_name, items.quantity, items.total_price FROM items " +
                "INNER JOIN products ON products.product_id = items.product_id " +
                "WHERE items.order_id = ? ";

        // show all items in user's own order
        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) { // May add product name for clarity
            statement.setInt(1, order_id);
            ResultSet resultSet = statement.executeQuery();

            out.println("========= Shopping Cart =========");
            out.println("Item No. | Product ID | Product Name | Quantity | Total Price");
            out.println("---------|------------|--------------|----------|------------");

            if (!resultSet.next()) {
                out.println("Your shopping cart is empty. Maybe you want to ...");
            } else {
                do {
                    //TODO spacing to be optimised
                    out.println(resultSet.getInt("NumOfItems") + " | "
                            + resultSet.getString("product_id") + " | "
                            + resultSet.getString("product_name") + " | "
                            + resultSet.getString("quantity") + " | "
                            + resultSet.getString("total_price"));
                } while (resultSet.next());
            }

        } catch (SQLException e) {
            System.err.println("An error occurred: " + e.getMessage());
            out.println("showItemInfo: Error accessing database.");
        }
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
        out.println("Enter '\\d' to finish adding products."); // TODO maybe add a direct link to show shopping cart

        int targetOrderID = getTargetOrderID(currentUser);

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
        String query = "SELECT * FROM orders WHERE user_id = ?::uuid AND is_in_group = ? AND order_status = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
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
        int targetOrderID = getTargetOrderID(currentUser);


        // Search from all orders of the user
        // If there is an active order, return the order_id
        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM groups JOIN orders ON groups.group_id = orders.group_id " +
                     "WHERE orders.user_id = ?::uuid AND orders.order_status = ?")) {

            statement.setObject(1, currentUser.getUser_id());
            statement.setString(2, "Created");
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Group found: " + resultSet.getInt("group_id"));
                return new Data.Group(resultSet.getInt("group_id"), resultSet.getString("group_name"), resultSet.getObject("user_id", UUID.class));
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
    public static void dismissGroup(Data.Group targetGroup){
        int targetGroupID = targetGroup.getGroup_id();

        try (Connection connection = new Service().getConnection();
             // remove all orders that assigned this group as the group_id
             PreparedStatement removeOrdersAssignGroup = connection.prepareStatement("UPDATE orders SET group_id = NULL WHERE group_id = ?");) {
            removeOrdersAssignGroup.setInt(1, targetGroupID);

            // delete the group from groups table
            // because the group no longer exists

            PreparedStatement deleteGroup = connection.prepareStatement("DELETE FROM groups WHERE group_id = ?");
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
        //TODO show all members in the group
        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT user_name FROM users WHERE user_id IN (SELECT user_id FROM orders WHERE group_id = ?)")) {
            statement.setInt(1, Objects.requireNonNull(getTargetGroup(currentUser)).getGroup_id());
            ResultSet resultSet = statement.executeQuery();
            out.println("========= Group Members =========");
            out.println("User Name");
            out.println("---------");
            while (resultSet.next()) {
                out.println(resultSet.getString("user_name"));
            }
        } catch (SQLException e) {
            System.err.println("showGroupMembers Error: " + e.getMessage());
        }
    }


    /*
     * Show all groups within the house user lives in
     * include active groups only (Can join)
     */
    private static void showAllGroup(int house_id, BufferedReader in, PrintWriter out) {
        String query = "SELECT groups.group_id, groups.group_name, users.user_name FROM groups " +
                "INNER JOIN users ON groups.user_id = users.user_id " +
                "WHERE groups.house_id = ? AND groups.order_status = ?";

        //show all groups within the house user lives in
        try (Connection connection = new Service().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) { //TODO to be modified
            statement.setInt(1, house_id);
            statement.setString(2, "Created");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                out.println("Group ID | Group Name | Created by | Joined Users");
                out.println("---------|------------|------------|-------------");

                while (resultSet.next()) {
                    //TODO spacing to be optimised
                    out.println(resultSet.getInt("group_id") + " | "
                            + resultSet.getString("group_name") + " | "
                            + resultSet.getString("user_name") + " | "); //TODO joined users to be queried
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
                if (Toolkit.askContinue(in, out)){
                    // if user is the owner of the group, then print error message

                    Data.Group currentGroup = Objects.requireNonNull(getTargetGroup(currentUser));
                    int targetGroupID = currentGroup.getGroup_id();
                    UUID ownerID = currentGroup.getOwner_id();

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
                        return;
                    }

                    // if user is in a group, then leave the group
                    // reset the order_id to user's own group
                    PreparedStatement statement = connection.prepareStatement("UPDATE orders SET group_id = ? , is_in_group = ? WHERE user_id = ?::uuid AND is_in_group = true");
                    statement.setNull(1, Types.INTEGER); // set the group id to null
                    statement.setBoolean(2, false);
                    statement.setObject(3, currentUser.getUser_id());
                    statement.executeUpdate();
                    out.println("You have left the group.");
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
     * this method should show all items in the order in (2) cases
     * 1. if user is in a group, then show the items in his/her own order
     * 2. if the user is in a group, then show all items in the group
     *
     * To check if user is in a group, you may call isInGroup() method
     */
    public static void showShoppingCart(Data.User currentUser, BufferedReader in, PrintWriter out) {
        String query = "SELECT orders.order_id, orders.group_id, groups.group_name FROM orders " +
                "INNER JOIN groups ON orders.group_id = groups.group_id " +
                "WHERE orders.user_id = ?::uuid";

        try (Connection connection = new Service().getConnection();
             // check if user has created an order
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, currentUser.getUser_id());
            ResultSet resultSet = statement.executeQuery();

            // If uses is in a group, show the all orders within the group
            if (resultSet.next()) {
                System.out.println("User " + currentUser.getUser_id() + " is in a group.");
                out.println("You are already in " + resultSet.getString("group_name"));
                //Get item list of user's order
                // TODO get all users' order within the group
                showItemInfo(getTargetOrderID(currentUser), in, out);
                out.println("Press return to continue...");


            } else {
                //Show orders of the house
                // TODO showAllGroup(currentUser.getHouse_id(), in, out);
                //Choose to join or create an order
                boolean restart = true;
                while (restart) {
                    // TODO show all items in order
                    showItemInfo(getTargetOrderID(currentUser), in, out); // not sure if this is correct
                    out.println("1. My Group");
                    out.println("2. Back to Menu");
                    String option = in.readLine();
                    switch (option) {
                        case "1":
                            // check if user is in a group
                            if (isInGroup(connection, currentUser)) {
                                // if user is in a group, then show the group
                                //myGroup(connection, currentUser, in, out);
                                out.println("Press enter to continue...");
                                in.readLine();
                                // restart = false;
                            } else {
                                // if user is not in a group, then user can join a group
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
                                        restart = false;
                                        break;
                                    default:
                                        out.println("Invalid option. Please try again.");
                                }
                            }
                            break;
                        case "2":
                            restart = false;
                            break;
                        default:
                            out.println("Invalid option. Please try again.");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("An error occurred: " + e.getMessage());
            out.println("Error while showing shopping cart.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void orderManagement(Data.User currentUser, BufferedReader in, PrintWriter out) throws SQLException {

    }


    private static void myGroupPage(Connection connection, Data.User currentUser, BufferedReader in, PrintWriter out) {
        showAllGroup(currentUser.getHouse_id(), in, out);
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
                System.out.println("Creating a new order for user: " + currentUser.getUser_id());
                Data.Order Order = new Data.Order(currentUser);

                PreparedStatement statement = connection.prepareStatement("INSERT INTO orders (user_id, order_status) VALUES (?, ?)");
                statement.setObject(1, currentUser.getUser_id());
                statement.setString(2, "Created"); // set order status as created
                statement.executeUpdate();
                System.out.println("Order.createOrder: Order created for user.");
            }
        } catch (SQLException e) {
            System.err.println("createOrder Error: " + e.getMessage());
        }
    }

}
