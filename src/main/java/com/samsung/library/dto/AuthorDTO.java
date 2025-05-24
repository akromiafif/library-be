package com.samsung.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class AuthorDTO {
    private Long id;

    @NotBlank(message = "Author name is required")
    @Size(max = 100, message = "Author name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Biography must not exceed 500 characters")
    private String biography;

    private Integer birthYear;

    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    private String nationality;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BookSummaryDTO> books;

    // Constructors
    public AuthorDTO() {}

    public AuthorDTO(String name, String biography, Integer birthYear, String nationality) {
        this.name = name;
        this.biography = biography;
        this.birthYear = birthYear;
        this.nationality = nationality;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<BookSummaryDTO> getBooks() { return books; }
    public void setBooks(List<BookSummaryDTO> books) { this.books = books; }
}