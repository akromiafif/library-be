// BorrowedBookService.java
package com.samsung.library.service;

import com.samsung.library.dto.BorrowedBookDTO;
import com.samsung.library.model.Book;
import com.samsung.library.model.BorrowedBook;
import com.samsung.library.model.BorrowStatus;
import com.samsung.library.model.Member;
import com.samsung.library.model.MembershipStatus;
import com.samsung.library.repository.BookRepository;
import com.samsung.library.repository.BorrowedBookRepository;
import com.samsung.library.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowedBookService {

    @Autowired
    private BorrowedBookRepository borrowedBookRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    // Configuration constants
    private static final int DEFAULT_BORROW_DAYS = 14;
    private static final int MAX_BOOKS_PER_MEMBER = 5;
    private static final double FINE_PER_DAY = 1.0; // $1 per day
    private static final int GRACE_PERIOD_DAYS = 1; // 1 day grace period before fines

    /**
     * Borrow a book - Creates a new borrowing record
     */
    public BorrowedBookDTO borrowBook(BorrowedBookDTO borrowedBookDTO) {
        // Step 1: Validate and get book
        Book book = validateAndGetBook(borrowedBookDTO.getBookId());

        // Step 2: Validate and get member
        Member member = validateAndGetMember(borrowedBookDTO.getMemberId());

        // Step 3: Perform borrowing business rules validation
        validateBorrowingRules(book, member);

        // Step 4: Create and save borrowing record
        BorrowedBook borrowedBook = createBorrowedBookRecord(book, member, borrowedBookDTO);
        BorrowedBook savedBorrowedBook = borrowedBookRepository.save(borrowedBook);

        // Step 5: Update book availability
        updateBookAvailability(book.getId(), -1);

        return convertToDTO(savedBorrowedBook);
    }

    /**
     * Return a book - Updates borrowing record and calculates fines
     */
    public BorrowedBookDTO returnBook(Long borrowedBookId) {
        // Get borrowing record with full details
        BorrowedBook borrowedBook = borrowedBookRepository.findByIdWithDetails(borrowedBookId)
                .orElseThrow(() -> new RuntimeException("Borrowed book record not found with ID: " + borrowedBookId));

        // Validate that book can be returned
        validateBookReturn(borrowedBook);

        // Process return
        LocalDate returnDate = LocalDate.now();
        borrowedBook.setReturnDate(returnDate);
        borrowedBook.setStatus(BorrowStatus.RETURNED);

        // Calculate and set fine if applicable
        Double fine = calculateFine(borrowedBook.getDueDate(), returnDate);
        borrowedBook.setFineAmount(fine);

        // Save updated record
        BorrowedBook updatedBorrowedBook = borrowedBookRepository.save(borrowedBook);

        // Update book availability
        updateBookAvailability(borrowedBook.getBook().getId(), 1);

        return convertToDTO(updatedBorrowedBook);
    }

    /**
     * Get borrowed book by ID with full details
     */
    @Transactional(readOnly = true)
    public Optional<BorrowedBookDTO> getBorrowedBookById(Long id) {
        return borrowedBookRepository.findByIdWithDetails(id)
                .map(this::convertToDTO);
    }

    /**
     * Get all borrowed books
     */
    @Transactional(readOnly = true)
    public List<BorrowedBookDTO> getAllBorrowedBooks() {
        return borrowedBookRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update borrowed book record
     */
    public BorrowedBookDTO updateBorrowedBook(Long id, BorrowedBookDTO borrowedBookDTO) {
        BorrowedBook existingRecord = borrowedBookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Borrowed book record not found with ID: " + id));

        // Update fields
        updateBorrowedBookFields(existingRecord, borrowedBookDTO);

        // Recalculate fine if return date changed
        if (borrowedBookDTO.getReturnDate() != null) {
            Double fine = calculateFine(existingRecord.getDueDate(), borrowedBookDTO.getReturnDate());
            existingRecord.setFineAmount(fine);
        }

        BorrowedBook updatedRecord = borrowedBookRepository.save(existingRecord);
        return convertToDTO(updatedRecord);
    }

    /**
     * Delete borrowed book record (admin function)
     */
    public boolean deleteBorrowedBook(Long id) {
        Optional<BorrowedBook> borrowedBookOpt = borrowedBookRepository.findByIdWithDetails(id);
        if (borrowedBookOpt.isEmpty()) {
            return false;
        }

        BorrowedBook borrowedBook = borrowedBookOpt.get();

        // If book is still borrowed, return it automatically
        if (borrowedBook.getStatus() == BorrowStatus.BORROWED) {
            updateBookAvailability(borrowedBook.getBook().getId(), 1);
        }

        borrowedBookRepository.deleteById(id);
        return true;
    }

    /**
     * Update overdue books status and calculate fines (scheduled task)
     */
    public void updateOverdueBooks() {
        List<BorrowedBook> overdueBooks = borrowedBookRepository.findOverdueBooks(LocalDate.now());

        for (BorrowedBook borrowedBook : overdueBooks) {
            borrowedBook.setStatus(BorrowStatus.OVERDUE);

            // Calculate current fine
            Double currentFine = calculateFine(borrowedBook.getDueDate(), LocalDate.now());
            borrowedBook.setFineAmount(currentFine);
        }

        if (!overdueBooks.isEmpty()) {
            borrowedBookRepository.saveAll(overdueBooks);
            System.out.println("Updated " + overdueBooks.size() + " overdue books");
        }
    }

    /**
     * Calculate total outstanding fines for a member
     */
    @Transactional(readOnly = true)
    public Double calculateMemberOutstandingFines(Long memberId) {
        return borrowedBookRepository.calculateTotalFinesByMember(memberId);
    }

    // =============== PRIVATE HELPER METHODS ===============

    /**
     * Validate and retrieve book for borrowing
     */
    private Book validateAndGetBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book '" + book.getTitle() + "' is not available for borrowing. No copies available.");
        }

        return book;
    }

    /**
     * Validate and retrieve member for borrowing
     */
    private Member validateAndGetMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));

        if (member.getMembershipStatus() != MembershipStatus.ACTIVE) {
            throw new RuntimeException("Member '" + member.getName() + "' does not have an active membership");
        }

        return member;
    }

    /**
     * Validate borrowing business rules
     */
    private void validateBorrowingRules(Book book, Member member) {
        // Check if member already has this book borrowed
        boolean alreadyBorrowed = borrowedBookRepository.isBookCurrentlyBorrowedByMember(
                book.getId(), member.getId());
        if (alreadyBorrowed) {
            throw new RuntimeException("Member '" + member.getName() + "' has already borrowed this book");
        }

        // Check maximum books limit
        Long currentBorrowings = borrowedBookRepository.countCurrentBorrowingsByMember(member.getId());
        if (currentBorrowings >= MAX_BOOKS_PER_MEMBER) {
            throw new RuntimeException("Member '" + member.getName() + "' has reached the maximum limit of " +
                    MAX_BOOKS_PER_MEMBER + " borrowed books");
        }

        // Check if member has outstanding fines (optional business rule)
        Double outstandingFines = calculateMemberOutstandingFines(member.getId());
        if (outstandingFines > 50.0) { // $50 limit
            throw new RuntimeException("Member '" + member.getName() + "' has outstanding fines of $" +
                    String.format("%.2f", outstandingFines) + ". Please clear fines before borrowing more books.");
        }
    }

    /**
     * Create borrowed book record
     */
    private BorrowedBook createBorrowedBookRecord(Book book, Member member, BorrowedBookDTO dto) {
        BorrowedBook borrowedBook = new BorrowedBook();
        borrowedBook.setBook(book);
        borrowedBook.setMember(member);
        borrowedBook.setBorrowDate(dto.getBorrowDate() != null ? dto.getBorrowDate() : LocalDate.now());
        borrowedBook.setDueDate(dto.getDueDate() != null ?
                dto.getDueDate() : borrowedBook.getBorrowDate().plusDays(DEFAULT_BORROW_DAYS));
        borrowedBook.setStatus(BorrowStatus.BORROWED);
        borrowedBook.setFineAmount(0.0);
        borrowedBook.setNotes(dto.getNotes());

        return borrowedBook;
    }

    /**
     * Validate book return
     */
    private void validateBookReturn(BorrowedBook borrowedBook) {
        if (borrowedBook.getStatus() != BorrowStatus.BORROWED && borrowedBook.getStatus() != BorrowStatus.OVERDUE) {
            throw new RuntimeException("Book is not currently borrowed. Current status: " + borrowedBook.getStatus());
        }

        if (borrowedBook.getReturnDate() != null) {
            throw new RuntimeException("Book has already been returned on " + borrowedBook.getReturnDate());
        }
    }

    /**
     * Calculate fine amount based on due date and return date
     */
    private Double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (returnDate == null || !returnDate.isAfter(dueDate)) {
            return 0.0;
        }

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);

        // Apply grace period
        if (daysOverdue <= GRACE_PERIOD_DAYS) {
            return 0.0;
        }

        long chargeableDays = daysOverdue - GRACE_PERIOD_DAYS;
        return chargeableDays * FINE_PER_DAY;
    }

    /**
     * Update book availability
     */
    private void updateBookAvailability(Long bookId, int change) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));

        int newAvailableCopies = book.getAvailableCopies() + change;

        if (newAvailableCopies < 0) {
            throw new RuntimeException("Cannot reduce available copies below 0");
        }

        if (newAvailableCopies > book.getTotalCopies()) {
            throw new RuntimeException("Available copies cannot exceed total copies");
        }

        book.setAvailableCopies(newAvailableCopies);
        bookRepository.save(book);
    }

    /**
     * Update borrowed book fields from DTO
     */
    private void updateBorrowedBookFields(BorrowedBook borrowedBook, BorrowedBookDTO dto) {
        if (dto.getBorrowDate() != null) {
            borrowedBook.setBorrowDate(dto.getBorrowDate());
        }
        if (dto.getDueDate() != null) {
            borrowedBook.setDueDate(dto.getDueDate());
        }
        if (dto.getReturnDate() != null) {
            borrowedBook.setReturnDate(dto.getReturnDate());
        }
        if (dto.getStatus() != null) {
            borrowedBook.setStatus(dto.getStatus());
        }
        if (dto.getFineAmount() != null) {
            borrowedBook.setFineAmount(dto.getFineAmount());
        }
        if (dto.getNotes() != null) {
            borrowedBook.setNotes(dto.getNotes());
        }
    }

    /**
     * Convert entity to DTO with all related information
     */
    private BorrowedBookDTO convertToDTO(BorrowedBook borrowedBook) {
        BorrowedBookDTO dto = new BorrowedBookDTO();
        dto.setId(borrowedBook.getId());
        dto.setBookId(borrowedBook.getBook().getId());
        dto.setMemberId(borrowedBook.getMember().getId());
        dto.setBookTitle(borrowedBook.getBook().getTitle());
        dto.setAuthorName(borrowedBook.getBook().getAuthor().getName());
        dto.setMemberName(borrowedBook.getMember().getName());
        dto.setMemberEmail(borrowedBook.getMember().getEmail());
        dto.setBorrowDate(borrowedBook.getBorrowDate());
        dto.setDueDate(borrowedBook.getDueDate());
        dto.setReturnDate(borrowedBook.getReturnDate());
        dto.setStatus(borrowedBook.getStatus());
        dto.setFineAmount(borrowedBook.getFineAmount());
        dto.setNotes(borrowedBook.getNotes());
        dto.setCreatedAt(borrowedBook.getCreatedAt());
        dto.setUpdatedAt(borrowedBook.getUpdatedAt());
        return dto;
    }
}