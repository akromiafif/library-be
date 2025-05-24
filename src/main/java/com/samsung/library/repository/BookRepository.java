package com.samsung.library.repository;

import com.samsung.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Find books by title (case insensitive)
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Find books by category
    List<Book> findByCategoryIgnoreCase(String category);

    // Find books by author
    List<Book> findByAuthorId(Long authorId);

    // Find books by publishing year
    List<Book> findByPublishingYear(Integer year);

    // Find books by publishing year range
    List<Book> findByPublishingYearBetween(Integer startYear, Integer endYear);

    // Find books by ISBN
    Optional<Book> findByIsbn(String isbn);

    // Find available books (books with available copies > 0)
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    List<Book> findAvailableBooks();

    // Find books with low stock (available copies <= threshold)
    @Query("SELECT b FROM Book b WHERE b.availableCopies <= :threshold")
    List<Book> findBooksWithLowStock(@Param("threshold") Integer threshold);

    // Complex search query
    @Query("SELECT b FROM Book b JOIN b.author a WHERE " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:category IS NULL OR LOWER(b.category) = LOWER(:category)) AND " +
            "(:authorName IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))) AND " +
            "(:year IS NULL OR b.publishingYear = :year)")
    List<Book> searchBooks(@Param("title") String title,
                           @Param("category") String category,
                           @Param("authorName") String authorName,
                           @Param("year") Integer year);

    // Find books with author information
    @Query("SELECT b FROM Book b JOIN FETCH b.author WHERE b.id = :id")
    Optional<Book> findByIdWithAuthor(@Param("id") Long id);

    // Get all distinct categories
    @Query("SELECT DISTINCT b.category FROM Book b ORDER BY b.category")
    List<String> findAllCategories();

    // Count books by category
    @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category")
    List<Object[]> countBooksByCategory();
}