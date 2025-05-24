package com.samsung.library.dto;

import com.samsung.library.model.BorrowStatus;
import java.time.LocalDate;

public class BorrowedBookSummaryDTO {
    private Long id;
    private String bookTitle;
    private String authorName;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowStatus status;
    private Double fineAmount;

    // Constructors
    public BorrowedBookSummaryDTO() {}

    public BorrowedBookSummaryDTO(Long id, String bookTitle, String authorName,
                                  LocalDate borrowDate, LocalDate dueDate,
                                  LocalDate returnDate, BorrowStatus status, Double fineAmount) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.authorName = authorName;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.fineAmount = fineAmount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public BorrowStatus getStatus() { return status; }
    public void setStatus(BorrowStatus status) { this.status = status; }

    public Double getFineAmount() { return fineAmount; }
    public void setFineAmount(Double fineAmount) { this.fineAmount = fineAmount; }
}