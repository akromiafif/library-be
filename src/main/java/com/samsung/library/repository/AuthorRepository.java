package com.samsung.library.repository;
import com.samsung.library.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    // Find authors by name (case insensitive)
    List<Author> findByNameContainingIgnoreCase(String name);

    // Find authors by nationality
    List<Author> findByNationalityIgnoreCase(String nationality);

    // Custom query to find authors with their book count
    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id")
    Optional<Author> findByIdWithBooks(@Param("id") Long id);

    // Find authors who have written books in a specific category
    @Query("SELECT DISTINCT a FROM Author a JOIN a.books b WHERE b.category = :category")
    List<Author> findByBookCategory(@Param("category") String category);

    // in AuthorRepository
    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books")
    List<Author> findAllWithBooks();

    // Count books by author
    @Query("SELECT COUNT(b) FROM Book b WHERE b.author.id = :authorId")
    Long countBooksByAuthor(@Param("authorId") Long authorId);
}