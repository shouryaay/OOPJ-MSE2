package com.ecommerce.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.db.DBConnection;
import com.ecommerce.model.Order;

/**
 * Data Access Object: every SQL operation on the Orders table,
 * written with PreparedStatement.
 */
public class OrderDAO {

    /**
     * (a) Inserts one order. Returns false (instead of crashing) when the
     * order_id already exists, because order_id is the PRIMARY KEY.
     */
    public boolean insertOrder(Order o) throws SQLException {
        String sql = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, o.getOrderId());
            ps.setString(2, o.getCustomerName());
            ps.setString(3, o.getProductName());
            ps.setInt(4, o.getQuantity());
            ps.setDouble(5, o.getUnitPrice());
            ps.setString(6, o.getStatus());
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // duplicate order_id
        }
    }

    /** Returns every order, sorted by order_id. */
    public List<Order> getAllOrders() throws SQLException {
        String sql = "SELECT * FROM Orders ORDER BY order_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapRows(rs);
        }
    }

    /**
     * (b) Orders with the given status AND quantity &gt; 2,
     * sorted by unit_price in descending order.
     */
    public List<Order> getOrdersByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    /** Finds one order by id, or returns null if it does not exist. */
    public Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> list = mapRows(rs);
                return list.isEmpty() ? null : list.get(0);
            }
        }
    }

    /**
     * (c) Increases the quantity of an order by the given amount.
     * Returns false if no order has this id.
     */
    public boolean increaseQuantity(int orderId, int additionalQty) throws SQLException {
        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, additionalQty);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    /** (d) Updates the status of an existing order. */
    public boolean updateStatus(int orderId, String newStatus) throws SQLException {
        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    /** (e) Total value of all orders using SUM(quantity * unit_price). */
    public double getTotalOrderValue() throws SQLException {
        String sql = "SELECT SUM(quantity * unit_price) AS total_value FROM Orders";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble("total_value") : 0.0;
        }
    }

    /** (e) Deletes an order, but only when its status is Cancelled. */
    public boolean deleteCancelledOrder(int orderId) throws SQLException {
        String sql = "DELETE FROM Orders WHERE order_id = ? AND status = 'Cancelled'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Converts every row of a ResultSet into an Order object. */
    private List<Order> mapRows(ResultSet rs) throws SQLException {
        List<Order> orders = new ArrayList<>();
        while (rs.next()) {
            orders.add(new Order(
                    rs.getInt("order_id"),
                    rs.getString("customer_name"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price"),
                    rs.getString("status")));
        }
        return orders;
    }
}
