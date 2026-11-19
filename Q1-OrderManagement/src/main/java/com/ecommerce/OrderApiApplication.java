package com.ecommerce;

import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ecommerce.db.DBConnection;

/**
 * REST version of the Order Management System, for testing in Postman.
 * Run: Right-click > Run As > Java Application, then use http://localhost:8081/orders
 * (The console menu version is still OrderManagementApp.)
 */
@SpringBootApplication
public class OrderApiApplication {

    public static void main(String[] args) throws SQLException {
        DBConnection.initDatabase(); // create ecommerce_db and the Orders table if missing
        SpringApplication.run(OrderApiApplication.class, args);
    }
}
