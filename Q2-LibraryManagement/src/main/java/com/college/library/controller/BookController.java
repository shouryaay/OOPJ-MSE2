package com.college.library.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.college.library.model.Book;
import com.college.library.service.BookService;

/**
 * REST controller for book records. Base URL: http://localhost:8080/books
 */
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // (b) Add a new book -> 201 Created
    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        if (book.getId() == null) {
            return error(HttpStatus.BAD_REQUEST, "Book id is required");
        }
        String problem = bookService.validate(book);
        if (problem != null) {
            return error(HttpStatus.BAD_REQUEST, problem);
        }
        if (bookService.exists(book.getId())) {
            return error(HttpStatus.CONFLICT, "Book with id " + book.getId() + " already exists");
        }
        bookService.addBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    // (c) Retrieve all books -> 200 OK
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    // (c) Retrieve one book by id -> 200 OK or 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable int id) {
        Book book = bookService.getBookById(id);
        if (book == null) {
            return error(HttpStatus.NOT_FOUND, "Book not found with id " + id);
        }
        return ResponseEntity.ok(book);
    }

    // (d) Update title, author, category and availableCopies -> 200 OK or 404 Not Found
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable int id, @RequestBody Book book) {
        if (!bookService.exists(id)) {
            return error(HttpStatus.NOT_FOUND, "Book not found with id " + id);
        }
        String problem = bookService.validate(book);
        if (problem != null) {
            return error(HttpStatus.BAD_REQUEST, problem);
        }
        return ResponseEntity.ok(bookService.updateBook(id, book));
    }

    // (e) Delete a book -> 200 OK if deleted, 404 Not Found if missing
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBook(@PathVariable int id) {
        if (bookService.deleteBook(id)) {
            return ResponseEntity.ok(Map.of("status", 200, "message", "Book with id " + id + " deleted successfully"));
        }
        return error(HttpStatus.NOT_FOUND, "Book not found with id " + id);
    }

    // builds a JSON error body like {"status":404,"message":"..."}
    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "message", message));
    }
}
