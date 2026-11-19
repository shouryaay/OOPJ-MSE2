package com.college.library.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.college.library.model.Book;
import com.college.library.repository.BookRepository;

/**
 * Business logic layer: validation rules, then calls the repository.
 */
@Service
public class BookService {

    private final BookRepository bookRepository;

    // constructor dependency injection
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(int id) {
        return bookRepository.findById(id);
    }

    public boolean exists(int id) {
        return bookRepository.existsById(id);
    }

    /** Returns an error message if the book data is invalid, or null if it is valid. */
    public String validate(Book book) {
        if (book.getTitle() == null || book.getTitle().isBlank()) {
            return "Title is required";
        }
        if (book.getAuthor() == null || book.getAuthor().isBlank()) {
            return "Author is required";
        }
        if (book.getAvailableCopies() == null || book.getAvailableCopies() < 0) {
            return "availableCopies is required and cannot be negative";
        }
        return null;
    }

    public void addBook(Book book) {
        bookRepository.save(book);
    }

    /** Updates title, author, category and availableCopies; returns the updated book. */
    public Book updateBook(int id, Book book) {
        bookRepository.update(id, book);
        return bookRepository.findById(id);
    }

    /** Returns true if a book was deleted, false if no book had this id. */
    public boolean deleteBook(int id) {
        return bookRepository.deleteById(id) > 0;
    }
}
