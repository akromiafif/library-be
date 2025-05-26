package com.samsung.library.repository;

import com.samsung.library.model.BorrowedBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowedBookRepository extends JpaRepository<BorrowedBook, Long> {
    // Find overdue books
    @Query("SELECT bb FROM BorrowedBook bb WHERE bb.dueDate < :currentDate AND bb.status = 'BORROWED'")
    List<BorrowedBook> findOverdueBooks(@Param("currentDate") LocalDate currentDate);

    // Find borrowed book with full details
    @Query("SELECT bb FROM BorrowedBook bb " +
            "JOIN FETCH bb.book b " +
            "JOIN FETCH bb.member m " +
            "JOIN FETCH b.author a " +
            "WHERE bb.id = :id")
    Optional<BorrowedBook> findByIdWithDetails(@Param("id") Long id);

    // Check if a book is currently borrowed by a member
    @Query("SELECT COUNT(bb) > 0 FROM BorrowedBook bb WHERE bb.book.id = :bookId AND bb.member.id = :memberId AND bb.status = 'BORROWED'")
    boolean isBookCurrentlyBorrowedByMember(@Param("bookId") Long bookId, @Param("memberId") Long memberId);

    // Count current borrowings by member
    @Query("SELECT COUNT(bb) FROM BorrowedBook bb WHERE bb.member.id = :memberId AND bb.status = 'BORROWED'")
    Long countCurrentBorrowingsByMember(@Param("memberId") Long memberId);

    // Calculate total fines for a member
    @Query("SELECT COALESCE(SUM(bb.fineAmount), 0) FROM BorrowedBook bb WHERE bb.member.id = :memberId")
    Double calculateTotalFinesByMember(@Param("memberId") Long memberId);
}