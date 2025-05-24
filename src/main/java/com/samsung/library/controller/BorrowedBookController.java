// BorrowedBookController.java
package com.samsung.library.controller;

import com.samsung.library.dto.ApiResponseDTO;
import com.samsung.library.dto.BorrowedBookDTO;
import com.samsung.library.dto.SearchRequestDTO;
import com.samsung.library.model.BorrowStatus;
import com.samsung.library.service.BorrowedBookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing borrowed books operations
 * Handles all borrowing-related endpoints with comprehensive validation and error handling
 */
@RestController
@RequestMapping("/api/borrowed-books")
@CrossOrigin(origins = "*")
public class BorrowedBookController {

    @Autowired
    private BorrowedBookService borrowedBookService;

    /**
     * Borrow a book - Create a new borrowing record
     * POST /api/borrowed-books/borrow
     */
    @PostMapping("/borrow")
    public ResponseEntity<ApiResponseDTO<BorrowedBookDTO>> borrowBook(
            @Valid @RequestBody BorrowedBookDTO borrowedBookDTO) {
        try {
            BorrowedBookDTO borrowedBook = borrowedBookService.borrowBook(borrowedBookDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.success("Book borrowed successfully", borrowedBook));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.error("Failed to borrow book: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("An unexpected error occurred while borrowing the book"));
        }
    }

    /**
     * Return a book - Update borrowing record with return information
     * PUT /api/borrowed-books/{id}/return
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<ApiResponseDTO<BorrowedBookDTO>> returnBook(@PathVariable Long id) {
        try {
            BorrowedBookDTO returnedBook = borrowedBookService.returnBook(id);
            return ResponseEntity.ok(ApiResponseDTO.success("Book returned successfully", returnedBook));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.error("Failed to return book: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("An unexpected error occurred while returning the book"));
        }
    }

    /**
     * Extend due date for a borrowed book
     * PUT /api/borrowed-books/{id}/extend?days={extensionDays}
     */
    @PutMapping("/{id}/extend")
    public ResponseEntity<ApiResponseDTO<BorrowedBookDTO>> extendDueDate(
            @PathVariable Long id,
            @RequestParam @Min(value = 1, message = "Extension days must be at least 1")
            @Max(value = 14, message = "Extension days cannot exceed 14") int days) {
        try {
            BorrowedBookDTO extendedBook = borrowedBookService.extendDueDate(id, days);
            return ResponseEntity.ok(ApiResponseDTO.success(
                    "Due date extended by " + days + " day(s) successfully", extendedBook));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.error("Failed to extend due date: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("An unexpected error occurred while extending due date"));
        }
    }

    /**
     * Get all borrowed books with optional pagination
     * GET /api/borrowed-books?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<BorrowedBookDTO>>> getAllBorrowedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            List<BorrowedBookDTO> borrowedBooks = borrowedBookService.getAllBorrowedBooks();

            // Simple pagination (for demonstration - in production, use Spring Data's Pageable)
            int start = page * size;
            int end = Math.min(start + size, borrowedBooks.size());

            if (start >= borrowedBooks.size()) {
                return ResponseEntity.ok(ApiResponseDTO.success("No more borrowed books found", List.of()));
            }

            List<BorrowedBookDTO> paginatedList = borrowedBooks.subList(start, end);
            return ResponseEntity.ok(ApiResponseDTO.success(
                    "Retrieved " + paginatedList.size() + " borrowed books (page " + page + ")", paginatedList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve borrowed books: " + e.getMessage()));
        }
    }

    /**
     * Get borrowed book by ID with full details
     * GET /api/borrowed-books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BorrowedBookDTO>> getBorrowedBookById(@PathVariable Long id) {
        try {
            Optional<BorrowedBookDTO> borrowedBook = borrowedBookService.getBorrowedBookById(id);
            if (borrowedBook.isPresent()) {
                return ResponseEntity.ok(ApiResponseDTO.success("Borrowed book found", borrowedBook.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Borrowed book not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve borrowed book: " + e.getMessage()));
        }
    }

    /**
     * Update borrowed book record (admin function)
     * PUT /api/borrowed-books/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BorrowedBookDTO>> updateBorrowedBook(
            @PathVariable Long id,
            @Valid @RequestBody BorrowedBookDTO borrowedBookDTO) {
        try {
            BorrowedBookDTO updatedBorrowedBook = borrowedBookService.updateBorrowedBook(id, borrowedBookDTO);
            if (updatedBorrowedBook != null) {
                return ResponseEntity.ok(ApiResponseDTO.success("Borrowed book updated successfully", updatedBorrowedBook));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Borrowed book not found with ID: " + id));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.error("Failed to update borrowed book: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("An unexpected error occurred while updating borrowed book"));
        }
    }

    /**
     * Delete borrowed book record (admin function)
     * DELETE /api/borrowed-books/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteBorrowedBook(@PathVariable Long id) {
        try {
            boolean deleted = borrowedBookService.deleteBorrowedBook(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponseDTO.success("Borrowed book record deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Borrowed book not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to delete borrowed book: " + e.getMessage()));
        }
    }

    /**
     * Advanced search for borrowed books with multiple criteria
     * POST /api/borrowed-books/search
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<BorrowedBookDTO>>> searchBorrowedBooks(
            @RequestBody SearchRequestDTO searchRequest) {
        try {
            List<BorrowedBookDTO> borrowedBooks = borrowedBookService.searchBorrowedBooks(searchRequest);
            String message = borrowedBooks.isEmpty() ?
                    "No borrowed books found matching the criteria" :
                    "Found " + borrowedBooks.size() + " borrowed book(s) matching the criteria";
            return ResponseEntity.ok(ApiResponseDTO.success(message, borrowedBooks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to search borrowed books: " + e.getMessage()));
        }
    }

    /**
     * Quick search borrowed books by book title or member name
     * GET /api/borrowed-books/search?q={query}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<BorrowedBookDTO>>> quickSearchBorrowedBooks(
            @RequestParam("q") String query) {
        try {
            SearchRequestDTO searchRequest = new SearchRequestDTO();
            searchRequest.setBookTitle(query);
            searchRequest.setMemberName(query);

            List<BorrowedBookDTO> borrowedBooks = borrowedBookService.searchBorrowedBooks(searchRequest);
            return ResponseEntity.ok(ApiResponseDTO.success(
                    "Found " + borrowedBooks.size() + " result(s) for: " + query, borrowedBooks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to search borrowed books: " + e.getMessage()));
        }
    }

    /**
     * Get all borrowed books by a specific member
     * GET /api/borrowed-books/member/{memberId}
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponseDTO<List<BorrowedBookDTO>>> getBorrowedBooksByMember(
            @PathVariable Long memberId) {
        try {
            List<BorrowedBookDTO> borrowedBooks = borrowedBookService.getBorrowedBooksByMember(memberId);
            return ResponseEntity.ok(ApiResponseDTO.success(
                    "Retrieved " + borrowedBooks.size() + " borrowed book(s) for member", borrowedBooks));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve member's borrowed books: " + e.getMessage()));
        }
    }

    /**
     * Get currently borrowed books by a specific member (not yet returned)
     * GET /api/borrowed-books/member/{memberId}/current
     */
    @GetMapping("/member/{memberId}/current")
    public ResponseEntity<ApiResponseDTO<List<BorrowedBookDTO>>> getCurrentBorrowingsByMember(
            @PathVariable Long memberId) {
        try {
            List<BorrowedBookDTO> currentBorrowings = borrowedBookService.getCurrentBorrowingsByMember(memberId);
            return ResponseEntity.ok(ApiResponseDTO.success(
                    "Member has " + currentBorrowings.size() + " book(s) currently borrowed", currentBorrowings));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve member's current borrowings: " + e.getMessage()));
        }
    }

    /**
     * Calculate total outstanding fines for a member
     * GET /api/borrowed-books/member/{memberId}/fines
     */
    @GetMapping("/member/{memberId}/fines")
    public ResponseEntity<ApiResponseDTO<Double>> getMemberOutstandingFines(
            @PathVariable Long memberId) {
        try {
            Double outstandingFines = borrowedBookService.calculateMemberOutstandingFines(memberId);
            String message = outstandingFines > 0 ?
                    "Member has outstanding fines of $" + String.format("%.2f", outstandingFines) :
                    "Member has no outstanding fines";
            return ResponseEntity.ok(ApiResponseDTO.success(message, outstandingFines));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to calculate member's fines: " + e.getMessage()));
        }
    }

    /**
     * Get overdue books
     * GET /api/borrowed-books/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponseDTO<List<BorrowedBookDTO>>> getOverdueBooks() {
        try {
            List<BorrowedBookDTO> overdueBooks = borrowedBookService.getOverdueBooks();
            String message = overdueBooks.isEmpty() ?
                    "No overdue books found" :
                    "Found " + overdueBooks.size() + " overdue book(s)";
            return ResponseEntity.ok(ApiResponseDTO.success(message, overdueBooks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve overdue books: " + e.getMessage()));
        }
    }

    /**
     * Get books due today
     * GET /api/borrowed-books/due-today
     */
    @GetMapping("/due-today")
    public ResponseEntity<ApiResponseDTO<List<BorrowedBookDTO>>> getBooksDueToday() {
        try {
            List<BorrowedBookDTO> booksDueToday = borrowedBookService.getBooksDueToday();
            String message = booksDueToday.isEmpty() ?
                    "No books due today" :
                    booksDueToday.size() + " book(s) due today";
            return ResponseEntity.ok(ApiResponseDTO.success(message, booksDueToday));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve books due today: " + e.getMessage()));
        }
    }

    /**
     * Get books due within specified days
     * GET /api/borrowed-books/due-within?days=3
     */
    @GetMapping("/due-within")
    public ResponseEntity<ApiResponseDTO<List<BorrowedBookDTO>>> getBooksDueWithin(
            @RequestParam @Min(value = 1, message = "Days must be at least 1")
            @Max(value = 30, message = "Days cannot exceed 30") int days) {
        try {
            // Create search request for books due within specified days
            SearchRequestDTO searchRequest = new SearchRequestDTO();
            searchRequest.setStatus(BorrowStatus.BORROWED);

            List<BorrowedBookDTO> allBorrowed = borrowedBookService.searchBorrowedBooks(searchRequest);
            LocalDate targetDate = LocalDate.now().plusDays(days);

            List<BorrowedBookDTO> dueWithinDays = allBorrowed.stream()
                    .filter(book -> book.getDueDate() != null &&
                            !book.getDueDate().isAfter(targetDate) &&
                            !book.getDueDate().isBefore(LocalDate.now()))
                    .toList();

            String message = dueWithinDays.isEmpty() ?
                    "No books due within " + days + " day(s)" :
                    dueWithinDays.size() + " book(s) due within " + days + " day(s)";
            return ResponseEntity.ok(ApiResponseDTO.success(message, dueWithinDays));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve books due within specified days: " + e.getMessage()));
        }
    }

    /**
     * Get borrowed books by status
     * GET /api/borrowed-books/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponseDTO<List<BorrowedBookDTO>>> getBorrowedBooksByStatus(
            @PathVariable String status) {
        try {
            BorrowStatus borrowStatus;
            try {
                borrowStatus = BorrowStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDTO.error("Invalid status. Valid statuses are: BORROWED, RETURNED, OVERDUE, LOST, DAMAGED"));
            }

            SearchRequestDTO searchRequest = new SearchRequestDTO();
            searchRequest.setStatus(borrowStatus);

            List<BorrowedBookDTO> borrowedBooks = borrowedBookService.searchBorrowedBooks(searchRequest);
            return ResponseEntity.ok(ApiResponseDTO.success(
                    "Found " + borrowedBooks.size() + " book(s) with status: " + status.toUpperCase(), borrowedBooks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve books by status: " + e.getMessage()));
        }
    }

    /**
     * Get borrowed books by date range
     * GET /api/borrowed-books/date-range?start=2025-01-01&end=2025-12-31
     */
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponseDTO<List<BorrowedBookDTO>>> getBorrowedBooksByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            if (start.isAfter(end)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDTO.error("Start date cannot be after end date"));
            }

            if (start.isBefore(LocalDate.now().minusYears(5))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDTO.error("Start date cannot be more than 5 years in the past"));
            }

            // For demonstration, we'll filter from all borrowed books
            // In production, you'd add a date range method to the repository
            List<BorrowedBookDTO> allBorrowedBooks = borrowedBookService.getAllBorrowedBooks();
            List<BorrowedBookDTO> filteredBooks = allBorrowedBooks.stream()
                    .filter(book -> book.getBorrowDate() != null &&
                            !book.getBorrowDate().isBefore(start) &&
                            !book.getBorrowDate().isAfter(end))
                    .toList();

            return ResponseEntity.ok(ApiResponseDTO.success(
                    "Found " + filteredBooks.size() + " book(s) borrowed between " + start + " and " + end,
                    filteredBooks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve books by date range: " + e.getMessage()));
        }
    }

    /**
     * Update overdue books status (maintenance endpoint)
     * PUT /api/borrowed-books/maintenance/update-overdue
     */
    @PutMapping("/maintenance/update-overdue")
    public ResponseEntity<ApiResponseDTO<Void>> updateOverdueBooks() {
        try {
            borrowedBookService.updateOverdueBooks();
            return ResponseEntity.ok(ApiResponseDTO.success("Overdue books status updated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to update overdue books: " + e.getMessage()));
        }
    }

    /**
     * Get borrowing statistics
     * GET /api/borrowed-books/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponseDTO<BorrowingStatsDTO>> getBorrowingStatistics() {
        try {
            List<BorrowedBookDTO> allBorrowedBooks = borrowedBookService.getAllBorrowedBooks();

            BorrowingStatsDTO stats = new BorrowingStatsDTO();
            stats.setTotalBorrowings(allBorrowedBooks.size());
            stats.setCurrentlyBorrowed((int) allBorrowedBooks.stream()
                    .filter(book -> book.getStatus() == BorrowStatus.BORROWED)
                    .count());
            stats.setOverdueBooks((int) allBorrowedBooks.stream()
                    .filter(book -> book.getStatus() == BorrowStatus.OVERDUE)
                    .count());
            stats.setReturnedBooks((int) allBorrowedBooks.stream()
                    .filter(book -> book.getStatus() == BorrowStatus.RETURNED)
                    .count());
            stats.setTotalFinesCollected(allBorrowedBooks.stream()
                    .filter(book -> book.getFineAmount() != null)
                    .mapToDouble(BorrowedBookDTO::getFineAmount)
                    .sum());

            return ResponseEntity.ok(ApiResponseDTO.success("Borrowing statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve borrowing statistics: " + e.getMessage()));
        }
    }

    /**
     * Inner class for borrowing statistics
     */
    public static class BorrowingStatsDTO {
        private int totalBorrowings;
        private int currentlyBorrowed;
        private int overdueBooks;
        private int returnedBooks;
        private double totalFinesCollected;

        // Getters and setters
        public int getTotalBorrowings() { return totalBorrowings; }
        public void setTotalBorrowings(int totalBorrowings) { this.totalBorrowings = totalBorrowings; }

        public int getCurrentlyBorrowed() { return currentlyBorrowed; }
        public void setCurrentlyBorrowed(int currentlyBorrowed) { this.currentlyBorrowed = currentlyBorrowed; }

        public int getOverdueBooks() { return overdueBooks; }
        public void setOverdueBooks(int overdueBooks) { this.overdueBooks = overdueBooks; }

        public int getReturnedBooks() { return returnedBooks; }
        public void setReturnedBooks(int returnedBooks) { this.returnedBooks = returnedBooks; }

        public double getTotalFinesCollected() { return totalFinesCollected; }
        public void setTotalFinesCollected(double totalFinesCollected) { this.totalFinesCollected = totalFinesCollected; }
    }
}