package com.samsung.library.dto;

public class BookSummaryDTO {
    private Long id;
    private String title;
    private String category;
    private Integer publishingYear;
    private Integer availableCopies;
    private Integer totalCopies;

    // Constructors
    public BookSummaryDTO() {}

    public BookSummaryDTO(Long id, String title, String category, Integer publishingYear,
                          Integer availableCopies, Integer totalCopies) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.publishingYear = publishingYear;
        this.availableCopies = availableCopies;
        this.totalCopies = totalCopies;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPublishingYear() { return publishingYear; }
    public void setPublishingYear(Integer publishingYear) { this.publishingYear = publishingYear; }

    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }

    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }
}
