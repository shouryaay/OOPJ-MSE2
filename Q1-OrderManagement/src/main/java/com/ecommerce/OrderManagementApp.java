package com.ecommerce;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import com.ecommerce.dao.OrderDAO;
import com.ecommerce.db.DBConnection;
import com.ecommerce.model.Order;

/**
 * Menu-driven Order Management System using Java JDBC and MySQL.
 * Run: Right-click > Run As > Java Application.
 */
public class OrderManagementApp {

    /** Statuses accepted when updating an order (d). Cancelled is needed for (e). */
    private static final String[] VALID_STATUSES = {"Pending", "Shipped", "Delivered", "Cancelled"};

    private static final String LINE =
            "+-------+------------------+------------------+------+------------+------------+";
    private static final String HEADER =
            "| ID    | Customer         | Product          | Qty  | Unit Price | Status     |";

    private static final Scanner sc = new Scanner(System.in);
    private static final OrderDAO dao = new OrderDAO();

    public static void main(String[] args) {
        try {
            DBConnection.initDatabase();
            System.out.println("Connected to MySQL database 'ecommerce_db' successfully.");
        } catch (SQLException e) {
            System.out.println("Could not connect to MySQL: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println();
            System.out.println("========== ORDER MANAGEMENT SYSTEM ==========");
            System.out.println("1. Insert sample orders (a)");
            System.out.println("2. Add a new order (a)");
            System.out.println("3. View all orders");
            System.out.println("4. Search orders by status, quantity > 2 (b)");
            System.out.println("5. Increase quantity of an order (c)");
            System.out.println("6. Update order status (d)");
            System.out.println("7. Total order value & delete cancelled order (e)");
            System.out.println("0. Exit");
            int choice = readInt("Enter your choice: ");

            try {
                switch (choice) {
                    case 1 -> insertSampleOrders();
                    case 2 -> addOrder();
                    case 3 -> viewAll();
                    case 4 -> searchByStatus();
                    case 5 -> increaseQuantity();
                    case 6 -> updateStatus();
                    case 7 -> totalAndDelete();
                    case 0 -> {
                        System.out.println("Thank you! Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please enter 0-7.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    // (a) insert at least three records with PreparedStatement, skipping duplicate ids
    private static void insertSampleOrders() throws SQLException {
        Order[] samples = {
            new Order(101, "Aarav Sharma", "Laptop", 3, 55000.00, "Pending"),
            new Order(102, "Priya Verma", "Headphones", 5, 1999.00, "Pending"),
            new Order(103, "Rohan Gupta", "Smartphone", 4, 24999.00, "Shipped"),
            new Order(104, "Neha Singh", "Keyboard", 1, 899.00, "Pending"),
            new Order(105, "Karan Mehta", "Monitor", 2, 12500.00, "Cancelled")
        };
        int inserted = 0;
        for (Order o : samples) {
            if (dao.insertOrder(o)) {
                inserted++;
                System.out.println("Inserted order " + o.getOrderId());
            } else {
                System.out.println("Order ID " + o.getOrderId() + " already exists - skipped (duplicate).");
            }
        }
        System.out.println(inserted + " order(s) inserted.");
    }

    // (a) insert one order typed by the user
    private static void addOrder() throws SQLException {
        int id = readInt("Order ID: ");
        String customer = readLine("Customer name: ");
        String product = readLine("Product name: ");
        int qty = readInt("Quantity: ");
        double price = readDouble("Unit price: ");
        String status = readValidStatus("Status (Pending/Shipped/Delivered/Cancelled): ");
        if (qty <= 0 || price < 0) {
            System.out.println("Quantity must be positive and price cannot be negative.");
            return;
        }
        if (dao.insertOrder(new Order(id, customer, product, qty, price, status))) {
            System.out.println("Order " + id + " inserted successfully.");
        } else {
            System.out.println("Error: Order ID " + id + " already exists. Duplicate IDs are not allowed.");
        }
    }

    private static void viewAll() throws SQLException {
        printTable(dao.getAllOrders());
    }

    // (b)
    private static void searchByStatus() throws SQLException {
        String status = readLine("Enter order status: ");
        List<Order> list = dao.getOrdersByStatus(status);
        if (list.isEmpty()) {
            System.out.println("No orders found with status '" + status + "' and quantity > 2.");
        } else {
            System.out.println("Orders with status '" + status + "' and quantity > 2 (unit price high to low):");
            printTable(list);
        }
    }

    // (c)
    private static void increaseQuantity() throws SQLException {
        int id = readInt("Enter order ID: ");
        int extra = readInt("Enter additional quantity: ");
        if (extra <= 0) {
            System.out.println("Additional quantity must be greater than 0.");
            return;
        }
        if (dao.increaseQuantity(id, extra)) {
            System.out.println("Quantity updated. New details:");
            printTable(List.of(dao.getOrderById(id)));
        } else {
            System.out.println("Order with ID " + id + " does not exist.");
        }
    }

    // (d)
    private static void updateStatus() throws SQLException {
        int id = readInt("Enter order ID: ");
        Order order = dao.getOrderById(id);
        if (order == null) {
            System.out.println("Order with ID " + id + " does not exist.");
            return;
        }
        System.out.println("Current order details:");
        printTable(List.of(order));
        String input = readLine("Enter new status (Pending/Shipped/Delivered/Cancelled): ");
        String status = normalizeStatus(input);
        if (status == null) {
            System.out.println("Invalid status '" + input + "'. Status not updated.");
            return;
        }
        dao.updateStatus(id, status);
        System.out.println("Status updated successfully. Updated order details:");
        printTable(List.of(dao.getOrderById(id)));
    }

    // (e)
    private static void totalAndDelete() throws SQLException {
        System.out.printf("Total order value = SUM(quantity * unit_price) = Rs. %.2f%n", dao.getTotalOrderValue());
        int id = readInt("Enter order ID to delete: ");
        Order order = dao.getOrderById(id);
        if (order == null) {
            System.out.println("Cannot delete: order with ID " + id + " does not exist.");
        } else if (!"Cancelled".equalsIgnoreCase(order.getStatus())) {
            System.out.println("Cannot delete: order " + id + " has status '" + order.getStatus()
                    + "'. Only Cancelled orders can be deleted.");
        } else if (dao.deleteCancelledOrder(id)) {
            System.out.println("Cancelled order " + id + " deleted successfully.");
            System.out.printf("New total order value = Rs. %.2f%n", dao.getTotalOrderValue());
        }
    }

    // ---------- helpers ----------

    private static void printTable(List<Order> orders) {
        System.out.println(LINE);
        System.out.println(HEADER);
        System.out.println(LINE);
        for (Order o : orders) {
            System.out.println(o);
        }
        System.out.println(LINE);
    }

    /** Returns the status in standard form (e.g. "shipped" -> "Shipped"), or null if invalid. */
    private static String normalizeStatus(String input) {
        for (String s : VALID_STATUSES) {
            if (s.equalsIgnoreCase(input.trim())) {
                return s;
            }
        }
        return null;
    }

    private static String readValidStatus(String prompt) {
        while (true) {
            String s = normalizeStatus(readLine(prompt));
            if (s != null) {
                return s;
            }
            System.out.println("Invalid status. Use Pending, Shipped, Delivered or Cancelled.");
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
