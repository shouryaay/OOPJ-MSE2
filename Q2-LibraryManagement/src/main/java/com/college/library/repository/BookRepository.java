package com.college.library.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.college.library.model.Book;

/**
 * Database layer: all SQL for the books table, using Spring JdbcTemplate.
 */
@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    // RowMapper converts one row of the ResultSet into one Book object
    private final RowMapper<Book> bookRowMapper = (rs, rowNum) -> new Book(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("category"),
            rs.getInt("available_copies"));

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Book> findAll() {
        return jdbcTemplate.query("SELECT * FROM books ORDER BY id", bookRowMapper);
    }

    public Book findById(int id) {
        List<Book> list = jdbcTemplate.query("SELECT * FROM books WHERE id = ?", bookRowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean existsById(int id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public int save(Book book) {
        return jdbcTemplate.update(
                "INSERT INTO books (id, title, author, category, available_copies) VALUES (?, ?, ?, ?, ?)",
                book.getId(), book.getTitle(), book.getAuthor(), book.getCategory(), book.getAvailableCopies());
    }

    public int update(int id, Book book) {
        return jdbcTemplate.update(
                "UPDATE books SET title = ?, author = ?, category = ?, available_copies = ? WHERE id = ?",
                book.getTitle(), book.getAuthor(), book.getCategory(), book.getAvailableCopies(), id);
    }

    public int deleteById(int id) {
        return jdbcTemplate.update("DELETE FROM books WHERE id = ?", id);
    }
}
