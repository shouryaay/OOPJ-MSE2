package com.ecommerce.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * (a) Establishes the connection with the MySQL database ecommerce_db
 * and creates the Orders table if it does not exist yet.
 */
public class DBConnection {

    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "ecommerce_db";
    private static final String URL = SERVER_URL + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "shourya1234";

    private DBConnection() {
    }

    /** Returns a new connection to ecommerce_db. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Creates the database and the Orders table (run once at startup). */
    public static void initDatabase() throws SQLException {
        try (Connection con = DriverManager.getConnection(
                SERVER_URL + "?useSSL=false&allowPublicKeyRetrieval=true", USER, PASSWORD);
             Statement st = con.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        }
        try (Connection con = getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS Orders ("
                    + "order_id INT PRIMARY KEY, "
                    + "customer_name VARCHAR(100), "
                    + "product_name VARCHAR(100), "
                    + "quantity INT, "
                    + "unit_price DOUBLE, "
                    + "status VARCHAR(30))");
        }
    }
}
