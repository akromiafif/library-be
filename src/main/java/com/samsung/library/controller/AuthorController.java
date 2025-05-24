package com.samsung.library.controller;

import com.samsung.library.dto.ApiResponseDTO;
import com.samsung.library.dto.AuthorDTO;
import com.samsung.library.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "*")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    // Create a new author
    @PostMapping
    public ResponseEntity<ApiResponseDTO<AuthorDTO>> createAuthor(@Valid @RequestBody AuthorDTO authorDTO) {
        try {
            AuthorDTO createdAuthor = authorService.createAuthor(authorDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.success("Author created successfully", createdAuthor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.error("Failed to create author: " + e.getMessage()));
        }
    }

    // Get all authors
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<AuthorDTO>>> getAllAuthors() {
        try {
            List<AuthorDTO> authors = authorService.getAllAuthors();
            return ResponseEntity.ok(ApiResponseDTO.success("Authors retrieved successfully", authors));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve authors: " + e.getMessage()));
        }
    }

    // Get author by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AuthorDTO>> getAuthorById(@PathVariable Long id) {
        try {
            Optional<AuthorDTO> author = authorService.getAuthorById(id);
            if (author.isPresent()) {
                return ResponseEntity.ok(ApiResponseDTO.success("Author found", author.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Author not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve author: " + e.getMessage()));
        }
    }

    // Get author by ID with books
    @GetMapping("/{id}/books")
    public ResponseEntity<ApiResponseDTO<AuthorDTO>> getAuthorByIdWithBooks(@PathVariable Long id) {
        try {
            Optional<AuthorDTO> author = authorService.getAuthorByIdWithBooks(id);
            if (author.isPresent()) {
                return ResponseEntity.ok(ApiResponseDTO.success("Author with books found", author.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Author not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve author with books: " + e.getMessage()));
        }
    }

    // Update author
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AuthorDTO>> updateAuthor(
            @PathVariable Long id, @Valid @RequestBody AuthorDTO authorDTO) {
        try {
            AuthorDTO updatedAuthor = authorService.updateAuthor(id, authorDTO);
            if (updatedAuthor != null) {
                return ResponseEntity.ok(ApiResponseDTO.success("Author updated successfully", updatedAuthor));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Author not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.error("Failed to update author: " + e.getMessage()));
        }
    }

    // Delete author
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAuthor(@PathVariable Long id) {
        try {
            boolean deleted = authorService.deleteAuthor(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponseDTO.success("Author deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Author not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to delete author: " + e.getMessage()));
        }
    }

    // Search authors by name
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<AuthorDTO>>> searchAuthors(@RequestParam String name) {
        try {
            List<AuthorDTO> authors = authorService.searchAuthorsByName(name);
            return ResponseEntity.ok(ApiResponseDTO.success("Authors found", authors));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to search authors: " + e.getMessage()));
        }
    }

    // Get authors by nationality
    @GetMapping("/nationality/{nationality}")
    public ResponseEntity<ApiResponseDTO<List<AuthorDTO>>> getAuthorsByNationality(@PathVariable String nationality) {
        try {
            List<AuthorDTO> authors = authorService.findAuthorsByNationality(nationality);
            return ResponseEntity.ok(ApiResponseDTO.success("Authors found by nationality", authors));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve authors by nationality: " + e.getMessage()));
        }
    }
}