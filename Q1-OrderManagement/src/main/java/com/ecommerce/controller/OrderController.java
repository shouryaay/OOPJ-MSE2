package com.ecommerce.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dao.OrderDAO;
import com.ecommerce.model.Order;

/**
 * REST endpoints for the Orders table. Uses the same JDBC OrderDAO as the console menu.
 * Base URL: http://localhost:8081/orders
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final List<String> VALID_STATUSES = List.of("Pending", "Shipped", "Delivered", "Cancelled");

    private final OrderDAO dao = new OrderDAO();

    // (a) insert three+ sample orders with PreparedStatement, skipping duplicate ids
    @PostMapping("/sample")
    public ResponseEntity<Map<String, Object>> insertSampleOrders() throws SQLException {
        Order[] samples = {
            new Order(101, "Aarav Sharma", "Laptop", 3, 55000.00, "Pending"),
            new Order(102, "Priya Verma", "Headphones", 5, 1999.00, "Pending"),
            new Order(103, "Rohan Gupta", "Smartphone", 4, 24999.00, "Shipped"),
            new Order(104, "Neha Singh", "Keyboard", 1, 899.00, "Pending"),
            new Order(105, "Karan Mehta", "Monitor", 2, 12500.00, "Cancelled")
        };
        List<Integer> inserted = new ArrayList<>();
        List<Integer> duplicates = new ArrayList<>();
        for (Order o : samples) {
            if (dao.insertOrder(o)) {
                inserted.add(o.getOrderId());
            } else {
                duplicates.add(o.getOrderId());
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("inserted", inserted, "skippedDuplicates", duplicates));
    }

    // (a) insert one order -> 201, or 409 if the order_id already exists
    @PostMapping
    public ResponseEntity<?> addOrder(@RequestBody Order order) throws SQLException {
        String status = normalizeStatus(order.getStatus());
        if (order.getQuantity() <= 0 || order.getUnitPrice() < 0 || status == null) {
            return message(HttpStatus.BAD_REQUEST,
                    "quantity must be > 0, unitPrice >= 0 and status one of " + VALID_STATUSES);
        }
        order.setStatus(status);
        if (!dao.insertOrder(order)) {
            return message(HttpStatus.CONFLICT, "Order ID " + order.getOrderId() + " already exists");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // view all orders
    @GetMapping
    public List<Order> getAllOrders() throws SQLException {
        return dao.getAllOrders();
    }

    // view one order -> 200 or 404
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable int id) throws SQLException {
        Order order = dao.getOrderById(id);
        if (order == null) {
            return message(HttpStatus.NOT_FOUND, "Order with ID " + id + " does not exist");
        }
        return ResponseEntity.ok(order);
    }

    // (b) orders with the given status and quantity > 2, sorted by unit price (high to low)
    @GetMapping("/search")
    public List<Order> searchByStatus(@RequestParam String status) throws SQLException {
        return dao.getOrdersByStatus(status);
    }

    // (c) increase quantity by the given amount -> 200, or 404 if the order does not exist
    @PutMapping("/{id}/quantity")
    public ResponseEntity<?> increaseQuantity(@PathVariable int id, @RequestParam int add) throws SQLException {
        if (add <= 0) {
            return message(HttpStatus.BAD_REQUEST, "Additional quantity must be greater than 0");
        }
        if (!dao.increaseQuantity(id, add)) {
            return message(HttpStatus.NOT_FOUND, "Order with ID " + id + " does not exist");
        }
        return ResponseEntity.ok(dao.getOrderById(id));
    }

    // (d) update status only if the order exists and the status is valid; returns the updated order
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestParam String value) throws SQLException {
        if (dao.getOrderById(id) == null) {
            return message(HttpStatus.NOT_FOUND, "Order with ID " + id + " does not exist");
        }
        String status = normalizeStatus(value);
        if (status == null) {
            return message(HttpStatus.BAD_REQUEST, "Invalid status '" + value + "'. Use " + VALID_STATUSES);
        }
        dao.updateStatus(id, status);
        return ResponseEntity.ok(dao.getOrderById(id));
    }

    // (e) total order value using SUM(quantity * unit_price)
    @GetMapping("/total")
    public Map<String, Object> totalOrderValue() throws SQLException {
        return Map.of("totalOrderValue", dao.getTotalOrderValue());
    }

    // (e) delete only if the status is Cancelled
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable int id) throws SQLException {
        Order order = dao.getOrderById(id);
        if (order == null) {
            return message(HttpStatus.NOT_FOUND, "Cannot delete: order with ID " + id + " does not exist");
        }
        if (!"Cancelled".equalsIgnoreCase(order.getStatus())) {
            return message(HttpStatus.BAD_REQUEST, "Cannot delete: order " + id + " has status '"
                    + order.getStatus() + "'. Only Cancelled orders can be deleted.");
        }
        dao.deleteCancelledOrder(id);
        return message(HttpStatus.OK, "Cancelled order " + id + " deleted successfully");
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> databaseError(SQLException e) {
        return message(HttpStatus.INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> message(HttpStatus status, String text) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "message", text));
    }

    private String normalizeStatus(String input) {
        if (input == null) {
            return null;
        }
        for (String s : VALID_STATUSES) {
            if (s.equalsIgnoreCase(input.trim())) {
                return s;
            }
        }
        return null;
    }
}
