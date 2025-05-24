package com.samsung.library.controller;

import com.samsung.library.dto.ApiResponseDTO;
import com.samsung.library.dto.BookDTO;
import com.samsung.library.dto.SearchRequestDTO;
import com.samsung.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    private BookService bookService;

    // Create a new book
    @PostMapping
    public ResponseEntity<ApiResponseDTO<BookDTO>> createBook(@Valid @RequestBody BookDTO bookDTO) {
        try {
            BookDTO createdBook = bookService.createBook(bookDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.success("Book created successfully", createdBook));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.error("Failed to create book: " + e.getMessage()));
        }
    }

    // Get all books
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<BookDTO>>> getAllBooks() {
        try {
            List<BookDTO> books = bookService.getAllBooks();
            return ResponseEntity.ok(ApiResponseDTO.success("Books retrieved successfully", books));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve books: " + e.getMessage()));
        }
    }

    // Get book by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BookDTO>> getBookById(@PathVariable Long id) {
        try {
            Optional<BookDTO> book = bookService.getBookById(id);
            if (book.isPresent()) {
                return ResponseEntity.ok(ApiResponseDTO.success("Book found", book.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Book not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve book: " + e.getMessage()));
        }
    }

    // Update book
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BookDTO>> updateBook(
            @PathVariable Long id, @Valid @RequestBody BookDTO bookDTO) {
        try {
            BookDTO updatedBook = bookService.updateBook(id, bookDTO);
            if (updatedBook != null) {
                return ResponseEntity.ok(ApiResponseDTO.success("Book updated successfully", updatedBook));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Book not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.error("Failed to update book: " + e.getMessage()));
        }
    }

    // Delete book
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteBook(@PathVariable Long id) {
        try {
            boolean deleted = bookService.deleteBook(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponseDTO.success("Book deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Book not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to delete book: " + e.getMessage()));
        }
    }

    // Search books
    @PostMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<BookDTO>>> searchBooks(@RequestBody SearchRequestDTO searchRequest) {
        try {
            List<BookDTO> books = bookService.searchBooks(searchRequest);
            return ResponseEntity.ok(ApiResponseDTO.success("Books found", books));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to search books: " + e.getMessage()));
        }
    }

    // Get available books
    @GetMapping("/available")
    public ResponseEntity<ApiResponseDTO<List<BookDTO>>> getAvailableBooks() {
        try {
            List<BookDTO> books = bookService.getAvailableBooks();
            return ResponseEntity.ok(ApiResponseDTO.success("Available books retrieved", books));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve available books: " + e.getMessage()));
        }
    }

    // Get books by category
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponseDTO<List<BookDTO>>> getBooksByCategory(@PathVariable String category) {
        try {
            List<BookDTO> books = bookService.getBooksByCategory(category);
            return ResponseEntity.ok(ApiResponseDTO.success("Books found by category", books));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve books by category: " + e.getMessage()));
        }
    }

    // Get all categories
    @GetMapping("/categories")
    public ResponseEntity<ApiResponseDTO<List<String>>> getAllCategories() {
        try {
            List<String> categories = bookService.getAllCategories();
            return ResponseEntity.ok(ApiResponseDTO.success("Categories retrieved", categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve categories: " + e.getMessage()));
        }
    }
}