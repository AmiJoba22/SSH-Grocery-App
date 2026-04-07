package org.example;

import java.sql.*;

public class Service {
    private static final String DB_URL = Credentials.URL;
    private static final String DB_USER = Credentials.USERNAME;
    private static final String DB_PASSWORD = Credentials.PASSWORD;

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static ResultSet fetchUserInfo(String short_id) {
        String query = "SELECT * FROM users WHERE short_id = ?";
        ResultSet resultSet = null;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, short_id);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }

        return resultSet;
    }

    public static String showHouseInfo() {
        // show all houses from database
        try (Connection connection = new Service().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM houses")) {

            StringBuilder sb = new StringBuilder();
            sb.append("House ID | Address\n");
            sb.append("---------|--------\n");
            while (resultSet.next()) {
                sb.append(resultSet.getInt("house_id")).append("   | ")
                        .append(resultSet.getString("address")).append("\n");
            }

            System.out.println(sb);
            return sb.toString();

        } catch (SQLException e) {
            System.err.println("An error occurred: " + e.getMessage());
            return "Error accessing database.";
        }
    }
}